package com.github.yuu1111.controllermod.config;

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
@Config(modid = "controllermod", category = "controller")
public class ControllerConfig {

    /**
     * デッドゾーン (0.0 〜 1.0)
     * この値以下のスティック入力は無視される
     */
    @Config.Comment("Deadzone for analog sticks (0.0 - 1.0)")
    @Config.DefaultFloat(0.25f)
    @Config.RangeFloat(min = 0.0f, max = 0.5f)
    public static float deadzone = 0.25f;

    /**
     * 視点操作の感度
     */
    @Config.Comment("Look sensitivity for right stick")
    @Config.DefaultFloat(4.0f)
    @Config.RangeFloat(min = 1.0f, max = 20.0f)
    public static float lookSensitivity = 4.0f;

    /**
     * Y軸反転
     */
    @Config.Comment("Invert Y axis for look controls")
    @Config.DefaultBoolean(false)
    public static boolean invertY = false;

    /**
     * トリガーの閾値
     * この値を超えるとトリガーが押されたと判定される
     */
    @Config.Comment("Trigger threshold (0.0 - 1.0)")
    @Config.DefaultFloat(0.5f)
    @Config.RangeFloat(min = 0.1f, max = 0.9f)
    public static float triggerThreshold = 0.5f;
}
