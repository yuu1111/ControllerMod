package com.github.yuu1111.controllermod.input;

/**
 * コントローラーの個別バインド定義
 *
 * <p>
 * 1つのアクション（ジャンプ、攻撃など）に対するボタン/トリガーのバインドを表す。
 * デフォルト値と現在値を保持し、リセット機能を提供する。
 */
public class ControllerBinding {

    /** バインドの識別子 (例: "jump", "attack") */
    private final String id;

    /** 表示名のローカライズキー */
    private final String translationKey;

    /** カテゴリ (例: "movement", "combat") */
    private final String category;

    /** デフォルトのボタン/トリガーインデックス */
    private final int defaultButton;

    /** 現在のボタン/トリガーインデックス */
    private int currentButton;

    /** トリガー入力かどうか (RT/LTなど) */
    private final boolean isTrigger;

    /** ホールド動作かどうか (押している間有効) */
    private final boolean isHold;

    /**
     * コンストラクタ
     *
     * @param id             バインドID
     * @param translationKey ローカライズキー
     * @param category       カテゴリ
     * @param defaultButton  デフォルトボタン
     * @param isTrigger      トリガー入力か
     * @param isHold         ホールド動作か
     */
    public ControllerBinding(String id, String translationKey, String category, int defaultButton, boolean isTrigger,
        boolean isHold) {
        this.id = id;
        this.translationKey = translationKey;
        this.category = category;
        this.defaultButton = defaultButton;
        this.currentButton = defaultButton;
        this.isTrigger = isTrigger;
        this.isHold = isHold;
    }

    /**
     * ボタンバインド用の簡易コンストラクタ
     */
    public ControllerBinding(String id, String translationKey, String category, int defaultButton, boolean isHold) {
        this(id, translationKey, category, defaultButton, false, isHold);
    }

    /**
     * バインドIDを取得
     */
    public String getId() {
        return id;
    }

    /**
     * ローカライズキーを取得
     */
    public String getTranslationKey() {
        return translationKey;
    }

    /**
     * カテゴリを取得
     */
    public String getCategory() {
        return category;
    }

    /**
     * デフォルトボタンを取得
     */
    public int getDefaultButton() {
        return defaultButton;
    }

    /**
     * 現在のバインドボタンを取得
     */
    public int getButton() {
        return currentButton;
    }

    /**
     * バインドボタンを設定
     */
    public void setButton(int button) {
        this.currentButton = button;
    }

    /**
     * トリガー入力かどうか
     */
    public boolean isTrigger() {
        return isTrigger;
    }

    /**
     * ホールド動作かどうか
     */
    public boolean isHold() {
        return isHold;
    }

    /**
     * デフォルトから変更されているか
     */
    public boolean isModified() {
        return currentButton != defaultButton;
    }

    /**
     * デフォルトにリセット
     */
    public void reset() {
        this.currentButton = defaultButton;
    }

    /**
     * バインドされていないか (UNBOUND = -1)
     */
    public boolean isUnbound() {
        return currentButton == -1;
    }

    /**
     * バインドを解除
     */
    public void unbind() {
        this.currentButton = -1;
    }
}
