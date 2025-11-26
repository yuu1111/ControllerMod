package com.github.yuu1111.controllermod.controller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.settings.KeyBinding;

/**
 * コントローラー入力をMinecraftのアクションにマッピングするクラス
 *
 * <p>
 * SDL2から受け取った軸・ボタン入力を処理し、Minecraftのキーバインドや
 * プレイヤー操作に変換するデッドゾーン処理や感度調整も行う
 *
 * <h2>ボタンマッピング (Xbox レイアウト)</h2>
 * <ul>
 * <li>A: ジャンプ</li>
 * <li>B: スニーク</li>
 * <li>Y: インベントリ</li>
 * <li>RT: 攻撃/破壊</li>
 * <li>LT: 使用/設置</li>
 * <li>RB/LB: ホットバー切り替え</li>
 * <li>L3: ダッシュ</li>
 * <li>Start: ポーズメニュー</li>
 * </ul>
 *
 * @author yuu1111
 * @see ControllerHandler
 * @see <a href="https://wiki.libsdl.org/SDL2/SDL_GameControllerButton">SDL2 GameControllerButton</a>
 */
public class InputHandler {

    // ===== 定数 =====

    /** スティックのデッドゾーン (0.0 〜 1.0) */
    private static final float DEADZONE = 0.25f;

    /** 視点操作の感度 */
    private static final float LOOK_SENSITIVITY = 4.0f;

    /** トリガーの閾値 (この値を超えると入力として認識) */
    private static final float TRIGGER_THRESHOLD = 0.5f;

    // ===== SDL2 軸インデックス =====

    /** 左スティック X軸 */
    public static final int AXIS_LEFT_X = 0;

    /** 左スティック Y軸 */
    public static final int AXIS_LEFT_Y = 1;

    /** 右スティック X軸 */
    public static final int AXIS_RIGHT_X = 2;

    /** 右スティック Y軸 */
    public static final int AXIS_RIGHT_Y = 3;

    /** 左トリガー (LT/L2) */
    public static final int AXIS_TRIGGER_LEFT = 4;

    /** 右トリガー (RT/R2) */
    public static final int AXIS_TRIGGER_RIGHT = 5;

    // ===== SDL2 ボタンインデックス =====
    // https://wiki.libsdl.org/SDL2/SDL_GameControllerButton

    /** Aボタン (Xbox) / ×ボタン (PlayStation) */
    public static final int BUTTON_A = 0;

    /** Bボタン (Xbox) / ○ボタン (PlayStation) */
    public static final int BUTTON_B = 1;

    /** Xボタン (Xbox) / □ボタン (PlayStation) */
    public static final int BUTTON_X = 2;

    /** Yボタン (Xbox) / △ボタン (PlayStation) */
    public static final int BUTTON_Y = 3;

    /** Back/Selectボタン (View) */
    public static final int BUTTON_BACK = 4;

    /** Guideボタン (Xboxボタン / PSボタン) */
    public static final int BUTTON_GUIDE = 5;

    /** Startボタン (Menu) */
    public static final int BUTTON_START = 6;

    /** 左スティック押込 (L3) */
    public static final int BUTTON_L3 = 7;

    /** 右スティック押込 (R3) */
    public static final int BUTTON_R3 = 8;

    /** 左バンパー (LB/L1) */
    public static final int BUTTON_LB = 9;

    /** 右バンパー (RB/R1) */
    public static final int BUTTON_RB = 10;

    /** D-Pad 上 */
    public static final int BUTTON_DPAD_UP = 11;

    /** D-Pad 下 */
    public static final int BUTTON_DPAD_DOWN = 12;

    /** D-Pad 左 */
    public static final int BUTTON_DPAD_LEFT = 13;

    /** D-Pad 右 */
    public static final int BUTTON_DPAD_RIGHT = 14;

    // ===== 軸の状態 =====

    private float leftStickX = 0;
    private float leftStickY = 0;
    private float rightStickX = 0;
    private float rightStickY = 0;
    private float triggerLeft = 0;
    private float triggerRight = 0;

    // ===== ボタンの状態 =====

    /** 現在フレームのボタン状態 */
    private boolean[] buttonStates = new boolean[16];

    /** 前フレームのボタン状態 (JustPressed判定用) */
    private boolean[] prevButtonStates = new boolean[16];

    /**
     * 軸の値を更新する
     *
     * <p>
     * スティック軸にはデッドゾーン処理を適用する
     * トリガー軸はそのまま保存する
     *
     * @param axisCode SDL2軸コード ({@link #AXIS_LEFT_X} など)
     * @param value    軸の値 (-1.0 〜 1.0)
     */
    public void updateAxis(int axisCode, float value) {
        switch (axisCode) {
            case AXIS_LEFT_X:
                leftStickX = applyDeadzone(value);
                break;
            case AXIS_LEFT_Y:
                leftStickY = applyDeadzone(value);
                break;
            case AXIS_RIGHT_X:
                rightStickX = applyDeadzone(value);
                break;
            case AXIS_RIGHT_Y:
                rightStickY = applyDeadzone(value);
                break;
            case AXIS_TRIGGER_LEFT:
                triggerLeft = value;
                break;
            case AXIS_TRIGGER_RIGHT:
                triggerRight = value;
                break;
        }
    }

    /**
     * ボタンの状態を更新する
     *
     * @param buttonCode SDL2ボタンコード ({@link #BUTTON_A} など)
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
     * GUIが開いている場合は全ての入力を解放する
     */
    public void applyMovement() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.currentScreen != null) {
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
     * ボタン入力をMinecraftアクションにマッピングする
     *
     * @param mc Minecraftインスタンス
     */
    private void applyButtons(Minecraft mc) {
        // A: ジャンプ
        setKeyState(mc.gameSettings.keyBindJump, buttonStates[BUTTON_A]);

        // B: スニーク (ホールド)
        setKeyState(mc.gameSettings.keyBindSneak, buttonStates[BUTTON_B]);

        // Y: インベントリ (押した瞬間のみ)
        if (isButtonJustPressed(BUTTON_Y)) {
            mc.thePlayer.openGui(null, 0, mc.theWorld, 0, 0, 0);
        }

        // L3: ダッシュ
        setKeyState(mc.gameSettings.keyBindSprint, buttonStates[BUTTON_L3]);

        // RT: 攻撃/破壊
        setKeyState(mc.gameSettings.keyBindAttack, triggerRight > TRIGGER_THRESHOLD);

        // LT: 使用/設置
        setKeyState(mc.gameSettings.keyBindUseItem, triggerLeft > TRIGGER_THRESHOLD);

        // RB: 次のホットバースロット
        if (isButtonJustPressed(BUTTON_RB)) {
            scrollHotbar(mc, 1);
        }

        // LB: 前のホットバースロット
        if (isButtonJustPressed(BUTTON_LB)) {
            scrollHotbar(mc, -1);
        }

        // Start: ポーズメニュー
        if (isButtonJustPressed(BUTTON_START)) {
            mc.displayGuiScreen(new GuiIngameMenu());
        }

        // R3: 視点切替 (F5相当)
        if (isButtonJustPressed(BUTTON_R3)) {
            toggleViewPerspective(mc);
        }

        // Back: プレイヤーリスト (Tabホールド)
        setKeyState(mc.gameSettings.keyBindPlayerList, buttonStates[BUTTON_BACK]);

        // D-Pad 上: 視点切替 (F5相当)
        if (isButtonJustPressed(BUTTON_DPAD_UP)) {
            toggleViewPerspective(mc);
        }

        // D-Pad 下: アイテムドロップ (Q)
        setKeyState(mc.gameSettings.keyBindDrop, buttonStates[BUTTON_DPAD_DOWN]);

        // D-Pad 左: (将来の拡張用に予約)

        // D-Pad 右: チャット画面を開く (T)
        if (isButtonJustPressed(BUTTON_DPAD_RIGHT)) {
            mc.displayGuiScreen(new GuiChat());
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
     * ボタンが今フレームで押されたか (JustPressed) をチェックする
     *
     * @param buttonCode チェックするボタンのコード
     * @return 今フレームで押された場合は {@code true}
     */
    private boolean isButtonJustPressed(int buttonCode) {
        return buttonStates[buttonCode] && !prevButtonStates[buttonCode];
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

        // X軸 → Yaw (左右回転)
        if (rightStickX != 0) {
            mc.thePlayer.rotationYaw += rightStickX * LOOK_SENSITIVITY;
        }

        // Y軸 → Pitch (上下回転)
        if (rightStickY != 0) {
            mc.thePlayer.rotationPitch += rightStickY * LOOK_SENSITIVITY;
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
        if (Math.abs(value) < DEADZONE) {
            return 0;
        }
        float sign = Math.signum(value);
        return sign * (Math.abs(value) - DEADZONE) / (1 - DEADZONE);
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
}
