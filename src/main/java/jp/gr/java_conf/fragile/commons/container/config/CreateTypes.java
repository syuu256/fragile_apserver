/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.container.config;

/**
 * 生成方法定義.
 */
public enum CreateTypes {

    /** Beanのタイプ属性 */
    SINGLETON("singleton", true),
    /** Beanのタイプ属性 */
    CREATE("create", false),
    /** Beanのタイプ属性 */
    @Deprecated
    PROTOTYPE("prototype", false),
    /** Beanのタイプ属性 */
    MBEAN("mbean", true),
    /** Beanのロード属性  */ // aspect設定はメソッド名称のみでパラメータ判定はしない
    ASPECT("aspect", true),
    /** Beanのタイプ属性 */
    JNDI("jndi", false),
    ;

    /** 値 */
    private final String attribute;

    private final boolean cache;

    /**
     * コンストラクタ.
     * @param attribute 名称
     * @param cache インスタンスキャッシュ
     */
    private CreateTypes(final String attribute, final boolean cache) {
        this.attribute = attribute;
        this.cache = cache;
    }

    /**
     * 定数取得.
     * @return 定数値
     */
    public String getLabel() {
        return attribute;
    }

    /**
     * シングルトン判定.
     * @return true = シングルトン
     */
    public boolean isCache() {
        return cache;
    }

    /**
     * 定数変換.
     * @param value 文字列
     * @return 変換値
     */
    public static CreateTypes toType(final String value) {

        for (final CreateTypes type : CreateTypes.values()) {
            if (type.getLabel().equals(value)) return type;
        }
        throw new IllegalArgumentException(value);
    }
}
