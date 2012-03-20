/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.container.config;

/**
 * 設定するクラスのタイプ.
 */
public enum TypeTypes {

    /** Beanのロード属性 */
    BEAN("bean"),
    /** Beanのロード属性 */
    DAO("dao"),
    /** Beanのロード属性 */
    INT("int"),
    /** Beanのロード属性 */
    LONG("long"),
    /** Beanのロード属性 */
    STAING("string"),
    /** Beanのロード属性 */
    BOOLEAN("boolean"),
    /** Beanのロード属性 */
    CHAR("char"),
    ;

    /** 値 */
    private final String attribute;

    /**
     * コンストラクタ.
     * @param attribute 属性文字列
     */
    private TypeTypes(final String attribute) {
        this.attribute = attribute;
    }

    /**
     * 定数取得.
     * @return 定数値
     */
    public String getLabel() {
        return attribute;
    }

    /**
     * 定数変換.
     * @param attribute 文字列
     * @return 変換値
     */
    public static TypeTypes toType(final String attribute) {

        for (final TypeTypes type : TypeTypes.values()) {
            if (type.getLabel().equals(attribute)) return type;
        }
        throw new IllegalArgumentException(attribute);
    }
}
