package com.github.yuu1111.controllermod.config;

import static com.github.yuu1111.controllermod.constants.Constants.MOD_ID;

import com.gtnewhorizon.gtnhlib.config.Config;

/**
 * コントローラーMODの設定クラス
 *
 * <p>
 * GTNHLibの@Configアノテーションを使用して、設定の定義と
 * GUI自動生成を行う。設定値はInputHandlerから参照される。
 *
 * @author yuu1111
 */
@Config(modid = MOD_ID, category = "controller")
public class ControllerConfig {

    /**
     * デッドゾーン (0.0 〜 1.0)
     * この値以下のスティック入力は無視される
     */
    @Config.LangKey("controllermod.config.deadzone")
    @Config.Comment("Deadzone for analog sticks (0.0 - 1.0)")
    @Config.DefaultFloat(0.25f)
    @Config.RangeFloat(min = 0.0f, max = 0.5f)
    public static float deadzone = 0.25f;

    /**
     * 視点操作の感度
     */
    @Config.LangKey("controllermod.config.lookSensitivity")
    @Config.Comment("Look sensitivity for right stick")
    @Config.DefaultFloat(4.0f)
    @Config.RangeFloat(min = 1.0f, max = 20.0f)
    public static float lookSensitivity = 4.0f;

    /**
     * Y軸反転
     */
    @Config.LangKey("controllermod.config.invertY")
    @Config.Comment("Invert Y axis for look controls")
    @Config.DefaultBoolean(false)
    public static boolean invertY = false;

    /**
     * トリガーの閾値
     * この値を超えるとトリガーが押されたと判定される
     */
    @Config.LangKey("controllermod.config.triggerThreshold")
    @Config.Comment("Trigger threshold (0.0 - 1.0)")
    @Config.DefaultFloat(0.5f)
    @Config.RangeFloat(min = 0.1f, max = 0.9f)
    public static float triggerThreshold = 0.5f;

    /**
     * バーチャルカーソルの移動速度
     * GUI画面でのスティック入力に対するカーソル移動量
     */
    @Config.LangKey("controllermod.config.cursorSpeed")
    @Config.Comment("Virtual cursor speed in GUI (pixels per tick)")
    @Config.DefaultFloat(8.0f)
    @Config.RangeFloat(min = 1.0f, max = 30.0f)
    public static float cursorSpeed = 8.0f;
}
