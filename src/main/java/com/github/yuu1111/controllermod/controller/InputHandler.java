package com.github.yuu1111.controllermod.controller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.settings.KeyBinding;

import com.github.yuu1111.controllermod.config.ControllerConfig;
import com.github.yuu1111.controllermod.constants.SDL2Constants;
import com.github.yuu1111.controllermod.gui.VirtualCursor;
import com.github.yuu1111.controllermod.gui.VirtualCursorManager;
import com.github.yuu1111.controllermod.input.ControllerBinding;
import com.github.yuu1111.controllermod.input.ControllerBindings;

/**
 * コントローラー入力をMinecraftのアクションにマッピングするクラス
 *
 * <p>
 * SDL2から受け取った軸・ボタン入力を処理し、Minecraftのキーバインドや
 * プレイヤー操作に変換する。デッドゾーン処理や感度調整も行う。
 *
 * <p>
 * ボタン/軸定数はControllerBindingsで定義。
 *
 * @see ControllerHandler
 * @see ControllerBindings
 */
public class InputHandler {

    // 軸の状態
    private float leftStickX = 0;
    private float leftStickY = 0;
    private float rightStickX = 0;
    private float rightStickY = 0;
    private float triggerLeft = 0;
    private float triggerRight = 0;

    // ボタンの状態
    private boolean[] buttonStates = new boolean[16];
    private boolean[] prevButtonStates = new boolean[16];

    /** バーチャルカーソル (GUI操作用) */
    private final VirtualCursor virtualCursor;

    /**
     * コンストラクタ
     *
     * <p>
     * バーチャルカーソルを初期化し、VirtualCursorManagerに登録する。
     */
    public InputHandler() {
        virtualCursor = new VirtualCursor();
        // MixinからアクセスできるようにVirtualCursorManagerに登録
        VirtualCursorManager.setInstance(virtualCursor);
    }

    /**
     * 軸の値を更新する
     *
     * <p>
     * スティック軸にはデッドゾーン処理を適用する。
     * トリガー軸はそのまま保存する。
     *
     * @param axisCode SDL2軸コード ({@link SDL2Constants#AXIS_LEFT_X} など)
     * @param value    軸の値 (-1.0 〜 1.0)
     */
    public void updateAxis(int axisCode, float value) {
        switch (axisCode) {
            case SDL2Constants.AXIS_LEFT_X:
                leftStickX = applyDeadzone(value);
                break;
            case SDL2Constants.AXIS_LEFT_Y:
                leftStickY = applyDeadzone(value);
                break;
            case SDL2Constants.AXIS_RIGHT_X:
                rightStickX = applyDeadzone(value);
                break;
            case SDL2Constants.AXIS_RIGHT_Y:
                rightStickY = applyDeadzone(value);
                break;
            case SDL2Constants.AXIS_TRIGGER_LEFT:
                triggerLeft = value;
                break;
            case SDL2Constants.AXIS_TRIGGER_RIGHT:
                triggerRight = value;
                break;
        }
    }

    /**
     * ボタンの状態を更新する
     *
     * @param buttonCode SDL2ボタンコード
     * @param pressed    ボタンが押されているかどうか
     */
    public void updateButton(int buttonCode, boolean pressed) {
        if (buttonCode >= 0 && buttonCode < buttonStates.length) {
            buttonStates[buttonCode] = pressed;
        }
    }

    /**
     * コントローラー入力をMinecraftに適用する
     *
     * <p>
     * 毎ティック呼び出され、以下の処理を行う:
     * <ol>
     * <li>左スティック → 移動 (WASD)</li>
     * <li>右スティック → 視点操作</li>
     * <li>各種ボタン → 対応するアクション</li>
     * </ol>
     *
     * <p>
     * GUIが開いている場合はバーチャルカーソルモードに切り替える。
     */
    public void applyMovement() {
        Minecraft mc = Minecraft.getMinecraft();

        // GUI が開いている場合はバーチャルカーソルで操作
        if (mc.currentScreen != null) {
            releaseAllMovement();
            releaseAllButtons();

            // バーチャルカーソルを更新
            virtualCursor.update(
                leftStickX,
                leftStickY,
                isBindingPressed(ControllerBindings.GUI_SELECT),
                isBindingPressed(ControllerBindings.GUI_BACK));

            // GUI でも一部のボタンは処理する
            applyGuiButtons(mc);

            // 前フレームのボタン状態を保存
            System.arraycopy(buttonStates, 0, prevButtonStates, 0, buttonStates.length);
            return;
        }

        if (mc.thePlayer == null) {
            releaseAllMovement();
            releaseAllButtons();
            return;
        }

        // 左スティック → 移動
        setKeyState(mc.gameSettings.keyBindForward, leftStickY < -0.1f);
        setKeyState(mc.gameSettings.keyBindBack, leftStickY > 0.1f);
        setKeyState(mc.gameSettings.keyBindLeft, leftStickX < -0.1f);
        setKeyState(mc.gameSettings.keyBindRight, leftStickX > 0.1f);

        // 右スティック → 視点操作
        applyLook(mc);

        // ボタン → 各種アクション
        applyButtons(mc);

        // 前フレームのボタン状態を保存
        System.arraycopy(buttonStates, 0, prevButtonStates, 0, buttonStates.length);
    }

    /**
     * バインドが押されているかチェック
     */
    private boolean isBindingPressed(ControllerBinding binding) {
        if (binding.isUnbound()) {
            return false;
        }
        int button = binding.getButton();
        // トリガーの場合
        if (button == SDL2Constants.TRIGGER_LEFT) {
            return triggerLeft > ControllerConfig.triggerThreshold;
        }
        if (button == SDL2Constants.TRIGGER_RIGHT) {
            return triggerRight > ControllerConfig.triggerThreshold;
        }
        // 通常ボタン
        if (button >= 0 && button < buttonStates.length) {
            return buttonStates[button];
        }
        return false;
    }

    /**
     * バインドが今フレームで押されたか (JustPressed)
     */
    private boolean isBindingJustPressed(ControllerBinding binding) {
        if (binding.isUnbound()) {
            return false;
        }
        int button = binding.getButton();
        // トリガーの場合
        if (button == SDL2Constants.TRIGGER_LEFT) {
            // トリガーのJustPressedは非対応 (ホールドのみ)
            return false;
        }
        if (button == SDL2Constants.TRIGGER_RIGHT) {
            return false;
        }
        // 通常ボタン
        if (button >= 0 && button < buttonStates.length) {
            return buttonStates[button] && !prevButtonStates[button];
        }
        return false;
    }

    /**
     * ボタン入力をMinecraftアクションにマッピングする
     *
     * @param mc Minecraftインスタンス
     */
    private void applyButtons(Minecraft mc) {
        // ジャンプ
        setKeyState(mc.gameSettings.keyBindJump, isBindingPressed(ControllerBindings.JUMP));

        // スニーク (ホールド)
        setKeyState(mc.gameSettings.keyBindSneak, isBindingPressed(ControllerBindings.SNEAK));

        // インベントリ (押した瞬間のみ)
        if (isBindingJustPressed(ControllerBindings.INVENTORY)) {
            mc.displayGuiScreen(new net.minecraft.client.gui.inventory.GuiInventory(mc.thePlayer));
        }

        // ダッシュ
        setKeyState(mc.gameSettings.keyBindSprint, isBindingPressed(ControllerBindings.SPRINT));

        // 攻撃/破壊
        setKeyState(mc.gameSettings.keyBindAttack, isBindingPressed(ControllerBindings.ATTACK));

        // 使用/設置
        setKeyState(mc.gameSettings.keyBindUseItem, isBindingPressed(ControllerBindings.USE_ITEM));

        // 次のホットバースロット
        if (isBindingJustPressed(ControllerBindings.HOTBAR_NEXT)) {
            scrollHotbar(mc, 1);
        }

        // 前のホットバースロット
        if (isBindingJustPressed(ControllerBindings.HOTBAR_PREV)) {
            scrollHotbar(mc, -1);
        }

        // ポーズメニュー
        if (isBindingJustPressed(ControllerBindings.PAUSE)) {
            mc.displayGuiScreen(new GuiIngameMenu());
        }

        // 視点切替
        if (isBindingJustPressed(ControllerBindings.TOGGLE_PERSPECTIVE)) {
            toggleViewPerspective(mc);
        }

        // プレイヤーリスト (Tabホールド)
        setKeyState(mc.gameSettings.keyBindPlayerList, isBindingPressed(ControllerBindings.PLAYER_LIST));

        // アイテムドロップ
        setKeyState(mc.gameSettings.keyBindDrop, isBindingPressed(ControllerBindings.DROP_ITEM));

        // チャット画面を開く
        if (isBindingJustPressed(ControllerBindings.OPEN_CHAT)) {
            mc.displayGuiScreen(new GuiChat());
        }
    }

    /**
     * GUI画面でのボタン入力を処理する
     *
     * @param mc Minecraftインスタンス
     */
    private void applyGuiButtons(Minecraft mc) {
        // ポーズ: GUI を閉じる
        if (isBindingJustPressed(ControllerBindings.PAUSE)) {
            mc.thePlayer.closeScreen();
        }

        // ホットバー切り替え (インベントリ等で有用)
        if (isBindingJustPressed(ControllerBindings.HOTBAR_NEXT) && mc.thePlayer != null) {
            scrollHotbar(mc, 1);
        }
        if (isBindingJustPressed(ControllerBindings.HOTBAR_PREV) && mc.thePlayer != null) {
            scrollHotbar(mc, -1);
        }
    }

    /**
     * 視点モードを切り替える
     *
     * <p>
     * 一人称 → 三人称(後方) → 三人称(前方) → 一人称 とサイクルする
     *
     * @param mc Minecraftインスタンス
     */
    private void toggleViewPerspective(Minecraft mc) {
        mc.gameSettings.thirdPersonView++;
        if (mc.gameSettings.thirdPersonView > 2) {
            mc.gameSettings.thirdPersonView = 0;
        }
    }

    /**
     * ホットバーのスロットをスクロールする
     *
     * @param mc        Minecraftインスタンス
     * @param direction スクロール方向 (正: 次へ、負: 前へ)
     */
    private void scrollHotbar(Minecraft mc, int direction) {
        int newSlot = mc.thePlayer.inventory.currentItem + direction;
        if (newSlot < 0) {
            newSlot = 8;
        } else if (newSlot > 8) {
            newSlot = 0;
        }
        mc.thePlayer.inventory.currentItem = newSlot;
    }

    /**
     * 全てのボタン入力を解放する
     *
     * <p>
     * GUI表示時などに呼び出され、押しっぱなし状態を解除する
     */
    private void releaseAllButtons() {
        Minecraft mc = Minecraft.getMinecraft();
        setKeyState(mc.gameSettings.keyBindJump, false);
        setKeyState(mc.gameSettings.keyBindSneak, false);
        setKeyState(mc.gameSettings.keyBindSprint, false);
        setKeyState(mc.gameSettings.keyBindAttack, false);
        setKeyState(mc.gameSettings.keyBindUseItem, false);
    }

    /**
     * 右スティックによる視点操作を適用する
     *
     * @param mc Minecraftインスタンス
     */
    private void applyLook(Minecraft mc) {
        if (mc.thePlayer == null) {
            return;
        }

        float sensitivity = ControllerConfig.lookSensitivity;

        // X軸 → Yaw (左右回転)
        if (rightStickX != 0) {
            mc.thePlayer.rotationYaw += rightStickX * sensitivity;
        }

        // Y軸 → Pitch (上下回転)
        if (rightStickY != 0) {
            float pitchDelta = rightStickY * sensitivity;
            // Y軸反転設定
            if (ControllerConfig.invertY) {
                pitchDelta = -pitchDelta;
            }
            mc.thePlayer.rotationPitch += pitchDelta;
            // -90° 〜 90° に制限
            mc.thePlayer.rotationPitch = Math.max(-90.0f, Math.min(90.0f, mc.thePlayer.rotationPitch));
        }
    }

    /**
     * 全ての移動入力を解放する
     */
    private void releaseAllMovement() {
        Minecraft mc = Minecraft.getMinecraft();
        setKeyState(mc.gameSettings.keyBindForward, false);
        setKeyState(mc.gameSettings.keyBindBack, false);
        setKeyState(mc.gameSettings.keyBindLeft, false);
        setKeyState(mc.gameSettings.keyBindRight, false);
    }

    /**
     * キーバインドの状態を設定する
     *
     * @param keyBinding 対象のキーバインド
     * @param pressed    押下状態
     */
    private void setKeyState(KeyBinding keyBinding, boolean pressed) {
        KeyBinding.setKeyBindState(keyBinding.getKeyCode(), pressed);
    }

    /**
     * デッドゾーンを適用する
     *
     * <p>
     * デッドゾーン内の値は0に、デッドゾーン外の値は正規化する
     *
     * @param value 入力値 (-1.0 〜 1.0)
     * @return デッドゾーン適用後の値
     */
    private float applyDeadzone(float value) {
        float deadzone = ControllerConfig.deadzone;
        if (Math.abs(value) < deadzone) {
            return 0;
        }
        float sign = Math.signum(value);
        return sign * (Math.abs(value) - deadzone) / (1 - deadzone);
    }

    /**
     * 左スティックのX軸の値を取得する
     *
     * @return X軸の値 (-1.0 〜 1.0、デッドゾーン適用済み)
     */
    public float getLeftStickX() {
        return leftStickX;
    }

    /**
     * 左スティックのY軸の値を取得する
     *
     * @return Y軸の値 (-1.0 〜 1.0、デッドゾーン適用済み)
     */
    public float getLeftStickY() {
        return leftStickY;
    }

    /**
     * バーチャルカーソルを取得する
     *
     * @return バーチャルカーソルインスタンス
     */
    public VirtualCursor getVirtualCursor() {
        return virtualCursor;
    }
}
