package com.github.yuu1111.controllermod.controller;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.github.yuu1111.controllermod.ControllerMod;

import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;

/**
 * SDL2 (sdl2gdx経由) を使用してコントローラー入力を処理するハンドラークラス
 *
 * <p>
 * このクラスは {@link ControllerListener} を実装し、コントローラーの接続/切断、
 * ボタン入力、軸入力などのイベントを受け取り、{@link InputHandler} に委譲する
 *
 * @author yuu1111
 * @see InputHandler
 * @see SDL2ControllerManager
 */
public class ControllerHandler implements ControllerListener {

    /** SDL2コントローラーマネージャー */
    private SDL2ControllerManager controllerManager;

    /** 初期化済みフラグ */
    private boolean initialized = false;

    /** 入力ハンドラー */
    private final InputHandler inputHandler = new InputHandler();

    /**
     * コントローラーシステムを初期化する
     *
     * <p>
     * SDL2ControllerManagerを作成し、既に接続されているコントローラーに対して
     * リスナーを登録する初期化に失敗した場合はエラーログを出力する
     */
    public void init() {
        try {
            controllerManager = new SDL2ControllerManager();
            controllerManager.addListenerAndRunForConnectedControllers(this);
            initialized = true;
            ControllerMod.LOG.info("SDL2 Controller system initialized");
        } catch (Exception e) {
            ControllerMod.LOG.error("Failed to initialize SDL2 controller system", e);
        }
    }

    /**
     * コントローラーの状態を更新する
     *
     * <p>
     * 毎ティック呼び出され、コントローラーの入力状態をポーリングし、
     * {@link InputHandler#applyMovement()} を呼び出してMinecraftに入力を適用する
     */
    public void update() {
        if (!initialized || controllerManager == null) {
            return;
        }

        try {
            controllerManager.pollState();
            inputHandler.applyMovement();
        } catch (Exception e) {
            ControllerMod.LOG.error("Error polling controller state", e);
        }
    }

    /**
     * コントローラーシステムをシャットダウンする
     *
     * <p>
     * SDL2ControllerManagerを閉じ、リソースを解放する
     */
    public void shutdown() {
        if (controllerManager != null) {
            controllerManager.close();
            controllerManager = null;
            initialized = false;
            ControllerMod.LOG.info("SDL2 Controller system shut down");
        }
    }

    /**
     * コントローラーシステムが初期化済みかどうかを返す
     *
     * @return 初期化済みの場合は {@code true}
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * コントローラーが接続された時に呼び出される
     *
     * @param controller 接続されたコントローラー
     */
    @Override
    public void connected(Controller controller) {
        ControllerMod.LOG.info("Controller connected: {}", controller.getName());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * コントローラーが切断された時に呼び出される
     *
     * @param controller 切断されたコントローラー
     */
    @Override
    public void disconnected(Controller controller) {
        ControllerMod.LOG.info("Controller disconnected: {}", controller.getName());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * ボタンが押された時に呼び出される
     *
     * @param controller ボタンが押されたコントローラー
     * @param buttonCode SDL2ボタンコード
     * @return イベントを消費した場合は {@code true}
     */
    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        ControllerMod.LOG.info("Button DOWN: {} on {}", buttonCode, controller.getName());
        inputHandler.updateButton(buttonCode, true);
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * ボタンが離された時に呼び出される
     *
     * @param controller ボタンが離されたコントローラー
     * @param buttonCode SDL2ボタンコード
     * @return イベントを消費した場合は {@code true}
     */
    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        ControllerMod.LOG.info("Button UP: {} on {}", buttonCode, controller.getName());
        inputHandler.updateButton(buttonCode, false);
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * 軸の値が変化した時に呼び出される
     *
     * @param controller 軸が変化したコントローラー
     * @param axisCode   SDL2軸コード
     * @param value      軸の値 (-1.0 〜 1.0)
     * @return イベントを消費した場合は {@code true}
     */
    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        inputHandler.updateAxis(axisCode, value);
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * POV (十字キー) の状態が変化した時に呼び出される
     * 注: SDL2 GameControllerではD-Padはボタンとして扱われるため、通常は呼び出されない
     *
     * @param controller POVが変化したコントローラー
     * @param povCode    POVコード
     * @param value      POVの方向
     * @return イベントを消費した場合は {@code true}
     */
    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        if (value != PovDirection.center) {
            ControllerMod.LOG.info("POV {}: {} on {}", povCode, value, controller.getName());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return false;
    }
}
