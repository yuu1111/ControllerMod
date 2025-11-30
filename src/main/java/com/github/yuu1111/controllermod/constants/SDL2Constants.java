package com.github.yuu1111.controllermod.constants;

/**
 * SDL2 GameController APIの定数
 *
 * <p>
 * 軸とボタンのインデックスを定義する。
 * sdl2gdxライブラリ経由で使用される。
 *
 * @see <a href="https://wiki.libsdl.org/SDL2/CategoryGameController">SDL2 GameController API</a>
 */
public final class SDL2Constants {

    private SDL2Constants() {
        // ユーティリティクラス
    }

    // 軸インデックス
    public static final int AXIS_LEFT_X = 0;
    public static final int AXIS_LEFT_Y = 1;
    public static final int AXIS_RIGHT_X = 2;
    public static final int AXIS_RIGHT_Y = 3;
    public static final int AXIS_TRIGGER_LEFT = 4;
    public static final int AXIS_TRIGGER_RIGHT = 5;

    // ボタンインデックス
    public static final int BUTTON_A = 0;
    public static final int BUTTON_B = 1;
    public static final int BUTTON_X = 2;
    public static final int BUTTON_Y = 3;
    public static final int BUTTON_BACK = 4;
    public static final int BUTTON_GUIDE = 5;
    public static final int BUTTON_START = 6;
    public static final int BUTTON_L3 = 7;
    public static final int BUTTON_R3 = 8;
    public static final int BUTTON_LB = 9;
    public static final int BUTTON_RB = 10;
    public static final int BUTTON_DPAD_UP = 11;
    public static final int BUTTON_DPAD_DOWN = 12;
    public static final int BUTTON_DPAD_LEFT = 13;
    public static final int BUTTON_DPAD_RIGHT = 14;

    // トリガーの仮想ボタンインデックス (バインド設定用)
    public static final int TRIGGER_LEFT = 100;
    public static final int TRIGGER_RIGHT = 101;
}
