package com.github.yuu1111.controllermod.gui;

import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import com.github.yuu1111.controllermod.ControllerMod;
import com.github.yuu1111.controllermod.config.ControllerConfig;

import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * コントローラー用バーチャルカーソル
 *
 * <p>
 * GUI画面でスティック入力による操作を可能にする完全に独立したバーチャルカーソル。
 * 実際のマウスカーソルには一切影響を与えず、内部座標のみを管理する。
 *
 * <p>
 * クリック操作はreflectionでGuiScreenのprotectedメソッドを直接呼び出すことで実現。
 * FMLのReflectionHelperを使用して難読化環境でも動作する。
 *
 * <ul>
 * <li>左スティック: カーソル移動</li>
 * <li>Aボタン: 左クリック</li>
 * <li>Bボタン: 右クリック</li>
 * </ul>
 *
 * @author yuu1111
 */
public class VirtualCursor {

    /** カーソルX座標 (GUI座標系) */
    private float cursorX;

    /** カーソルY座標 (GUI座標系) */
    private float cursorY;

    /** カーソルが有効かどうか */
    private boolean active = false;

    /** 前フレームでGUIが開いていたか */
    private boolean wasGuiOpen = false;

    /** mouseClicked メソッド (reflection) */
    private Method mouseClickedMethod;

    /** mouseReleased メソッド (reflection) */
    private Method mouseReleasedMethod;

    /** mouseClickMove メソッド (reflection) */
    private Method mouseClickMoveMethod;

    /** 前フレームのAボタン状態 */
    private boolean prevButtonA = false;

    /** 前フレームのBボタン状態 */
    private boolean prevButtonB = false;

    /** マウスボタンが押されているか */
    private boolean mouseButtonHeld = false;

    /** 押されているマウスボタン (0=左, 1=右) */
    private int heldMouseButton = -1;

    /**
     * コンストラクタ
     *
     * <p>
     * GuiScreenのprotectedメソッドをreflectionで取得する。
     * FMLのReflectionHelperを使用して難読化環境でも動作するようにする。
     */
    public VirtualCursor() {
        try {
            // GuiScreen.mouseClicked(int x, int y, int button)
            // MCP名: mouseClicked, SRG名: func_73864_a
            mouseClickedMethod = ReflectionHelper.findMethod(
                GuiScreen.class,
                null,
                new String[] { "mouseClicked", "func_73864_a" },
                int.class,
                int.class,
                int.class);

            // GuiScreen.mouseReleased(int x, int y, int button)
            // MCP名: mouseReleased, SRG名: func_146286_b
            mouseReleasedMethod = ReflectionHelper.findMethod(
                GuiScreen.class,
                null,
                new String[] { "mouseReleased", "func_146286_b" },
                int.class,
                int.class,
                int.class);

            // GuiScreen.mouseClickMove(int x, int y, int button, long timeSinceLastClick)
            // MCP名: mouseClickMove, SRG名: func_146273_a
            mouseClickMoveMethod = ReflectionHelper.findMethod(
                GuiScreen.class,
                null,
                new String[] { "mouseClickMove", "func_146273_a" },
                int.class,
                int.class,
                int.class,
                long.class);

            ControllerMod.LOG.info("VirtualCursor: GuiScreen methods found via reflection");
        } catch (Exception e) {
            ControllerMod.LOG.error("Failed to get GuiScreen methods via reflection", e);
        }
    }

    /**
     * カーソルを更新する
     *
     * @param stickX  左スティックX軸 (-1.0 〜 1.0)
     * @param stickY  左スティックY軸 (-1.0 〜 1.0)
     * @param buttonA Aボタンが押されているか
     * @param buttonB Bボタンが押されているか
     */
    public void update(float stickX, float stickY, boolean buttonA, boolean buttonB) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen currentScreen = mc.currentScreen;

        // GUI が開いているかチェック
        boolean guiOpen = currentScreen != null;

        // GUI が開いた瞬間にカーソルを中央に初期化
        if (guiOpen && !wasGuiOpen) {
            initCursor(mc);
            active = true;
        }

        // GUI が閉じたらカーソルを無効化
        if (!guiOpen && wasGuiOpen) {
            active = false;
            mouseButtonHeld = false;
            heldMouseButton = -1;
        }

        wasGuiOpen = guiOpen;

        if (!active || currentScreen == null) {
            return;
        }

        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        // カーソル移動
        float speed = ControllerConfig.cursorSpeed;
        cursorX += stickX * speed;
        cursorY += stickY * speed;

        // 画面内に制限
        cursorX = Math.max(0, Math.min(screenWidth - 1, cursorX));
        cursorY = Math.max(0, Math.min(screenHeight - 1, cursorY));

        // ボタン処理 (reflectionでクリックを発生させる)
        handleButtons(currentScreen, buttonA, buttonB);

        // 前フレームの状態を保存
        prevButtonA = buttonA;
        prevButtonB = buttonB;
    }

    /**
     * カーソルを画面中央に初期化する
     */
    private void initCursor(Minecraft mc) {
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        cursorX = sr.getScaledWidth() / 2f;
        cursorY = sr.getScaledHeight() / 2f;
        mouseButtonHeld = false;
        heldMouseButton = -1;
        prevButtonA = false;
        prevButtonB = false;
    }

    /**
     * ボタン入力を処理する
     *
     * <p>
     * reflectionでGuiScreenのメソッドを直接呼び出す。
     * 実際のマウスカーソルは一切動かさない。
     */
    private void handleButtons(GuiScreen screen, boolean buttonA, boolean buttonB) {
        int x = (int) cursorX;
        int y = (int) cursorY;

        // A ボタン → 左クリック
        if (buttonA && !prevButtonA) {
            // マウスダウン
            simulateMouseClick(screen, x, y, 0);
            mouseButtonHeld = true;
            heldMouseButton = 0;
        } else if (!buttonA && prevButtonA) {
            // マウスアップ
            simulateMouseRelease(screen, x, y, 0);
            mouseButtonHeld = false;
            heldMouseButton = -1;
        }

        // B ボタン → 右クリック
        if (buttonB && !prevButtonB) {
            simulateMouseClick(screen, x, y, 1);
            mouseButtonHeld = true;
            heldMouseButton = 1;
        } else if (!buttonB && prevButtonB) {
            simulateMouseRelease(screen, x, y, 1);
            mouseButtonHeld = false;
            heldMouseButton = -1;
        }

        // ドラッグ中の処理
        if (mouseButtonHeld && heldMouseButton >= 0) {
            simulateMouseDrag(screen, x, y, heldMouseButton);
        }
    }

    /**
     * マウスクリックをシミュレートする
     */
    private void simulateMouseClick(GuiScreen screen, int x, int y, int button) {
        if (mouseClickedMethod == null) {
            ControllerMod.LOG.warn("mouseClickedMethod is null");
            return;
        }
        try {
            mouseClickedMethod.invoke(screen, x, y, button);
        } catch (Exception e) {
            ControllerMod.LOG.debug("Failed to simulate mouse click: {}", e.getMessage());
        }
    }

    /**
     * マウスリリースをシミュレートする
     */
    private void simulateMouseRelease(GuiScreen screen, int x, int y, int button) {
        if (mouseReleasedMethod == null) {
            return;
        }
        try {
            mouseReleasedMethod.invoke(screen, x, y, button);
        } catch (Exception e) {
            ControllerMod.LOG.debug("Failed to simulate mouse release: {}", e.getMessage());
        }
    }

    /**
     * マウスドラッグをシミュレートする
     */
    private void simulateMouseDrag(GuiScreen screen, int x, int y, int button) {
        if (mouseClickMoveMethod == null) {
            return;
        }
        try {
            mouseClickMoveMethod.invoke(screen, x, y, button, 0L);
        } catch (Exception e) {
            // ドラッグエラーは頻繁に発生する可能性があるのでログは出さない
        }
    }

    /**
     * マウスボタンが押されているかどうかを返す
     *
     * @param button マウスボタン (0=左, 1=右)
     * @return 押されている場合は {@code true}
     */
    public boolean isMouseButtonDown(int button) {
        return mouseButtonHeld && heldMouseButton == button;
    }

    /**
     * カーソルを描画する
     *
     * <p>
     * GUI描画後に呼び出す
     */
    public void render() {
        if (!active) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen == null) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(cursorX, cursorY, 300); // z=300 でGUIの上に描画

        // カーソルの色 (白、押下時は黄色)
        float r = 1.0f;
        float g = 1.0f;
        float b = mouseButtonHeld ? 0.0f : 1.0f;
        float a = 0.9f;

        // カーソルサイズ
        int size = 8;

        // シンプルな十字カーソルを描画
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glColor4f(r, g, b, a);

        Tessellator tessellator = Tessellator.instance;

        // 横線
        tessellator.startDrawingQuads();
        tessellator.addVertex(-size, -1, 0);
        tessellator.addVertex(-size, 1, 0);
        tessellator.addVertex(size, 1, 0);
        tessellator.addVertex(size, -1, 0);
        tessellator.draw();

        // 縦線
        tessellator.startDrawingQuads();
        tessellator.addVertex(-1, -size, 0);
        tessellator.addVertex(-1, size, 0);
        tessellator.addVertex(1, size, 0);
        tessellator.addVertex(1, -size, 0);
        tessellator.draw();

        // 中心点 (黒)
        GL11.glColor4f(0, 0, 0, 1.0f);
        tessellator.startDrawingQuads();
        tessellator.addVertex(-2, -2, 0);
        tessellator.addVertex(-2, 2, 0);
        tessellator.addVertex(2, 2, 0);
        tessellator.addVertex(2, -2, 0);
        tessellator.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    /**
     * カーソルが有効かどうかを返す
     *
     * @return カーソルが有効な場合は {@code true}
     */
    public boolean isActive() {
        return active;
    }

    /**
     * カーソルのX座標を取得する
     *
     * @return X座標 (GUI座標系)
     */
    public float getCursorX() {
        return cursorX;
    }

    /**
     * カーソルのY座標を取得する
     *
     * @return Y座標 (GUI座標系)
     */
    public float getCursorY() {
        return cursorY;
    }
}
