/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.container.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import jp.gr.java_conf.fragile.commons.collect.Collect;

/**
 * Bean定義クラス.<br />
 */
public class BeanMetaData implements Serializable {

    /** シリアルバージョンID */
    private static final long serialVersionUID = 8941555997768345356L;

    /** 設定値がnull */
    public static final String EMPTY_STRING = "null";

    /** タブ */
    public static final String TAB = "  ";

// =========================================================

    /** BeanID */
    private String beanID = null;

    /** 生成方法 */
    private CreateTypes createTypes = null;

    /** 生成するクラスのタイプ */
    private TypeTypes typeTypes = null;

    /** ロードするソース */
    private String src = "";

// =========================================================

    /** セッターインジェクション key = メソッド名  val = 参照IDのカンマ区切り  */ // add系を考慮してカンマ区切定義
    private Map<String, String> injection = Collect.newHashMap();

    /** アスペクト key = メソッド名  val = 参照IDのカンマ区切り  */ // 複数の設定は可能にする
    private Map<String, String> aspect = Collect.newHashMap();

// =================================================

    /**
     * bean定義ID。<br />
     * @param value bean定義ID
     * @return 自インスタンス
     */
    public BeanMetaData setBeanID(final String value) {
        beanID = value;
        return this;
    }

    /**
     * bean定義ID。<br />
     * @return bean定義ID
     */
    public String getBeanID() {
        return beanID;
    }

// =================================================

    /**
     * 生成方法.
     * @param value 生成方法文字列
     * @return 自インスタンス
     */
    public BeanMetaData setCreate(final String value) {
        createTypes = CreateTypes.toType(value);
        return this;
    }

    /**
     * 生成方法.
     * @return 生成方法
     */
    public CreateTypes getCreate() {
        return createTypes;
    }

// =================================================

    /**
     * 生成するクラスのタイプ.
     * @param value 生成するクラスのタイプ文字列
     * @return 自インスタンス
     */
    public BeanMetaData setType(final String value) {
        typeTypes = TypeTypes.toType(value);
        return this;
    }

    /**
     * 生成するクラスのタイプ.
     * @return ロード方式の設定
     */
    public TypeTypes getType() {
        return typeTypes;
    }

// =================================================

    /**
     * ロードするソース文字列。<br />
     * @param s ロードするソース文字列
     * @return 自インスタンス
     */
    public BeanMetaData setSrc(final String s) {
        src = s;
        return this;
    }

    /**
     * ロードするソース文字列。<br />
     * @return ロードするソース文字列
     */
    public String getSrc() {
        return src;
    }

// =================================================

    /**
     * Injectionタグ値.
     * @param method セットするメソッド名
     * @param id セットするBeanのbeanID(カンマ区切り)
     * @return 自インスタンス
     * 例外 IllegalStateException 一度でもgetSettersが実行された場合
     */
    public BeanMetaData putInjections(final String method, final String id) {
        if (injection.put(method, id) != null)
            throw new IllegalStateException(beanID + ":" + method);
        return this;
    }

    /**
     * Injectionタグ値.
     * @return セッター集合コレクション(不変Map)
     */
    public Map<String, String> getInjections() {
        return Collections.unmodifiableMap(injection);
    }

// =================================================

    /**
     * aspectタグ値.
     * @param method セットするメソッド名
     * @param id セットするBeanのbeanID(カンマ区切り)
     * @return 自インスタンス
     * 例外 IllegalStateException 一度でもgetSettersが実行された場合
     */
    public BeanMetaData putAspects(final String method, final String id) {
        if (aspect.put(method, id) != null)
            throw new IllegalStateException(beanID + ":" + method);
        return this;
    }

    /**
     * aspectタグ値.
     * @return セッター集合コレクション(不変Map)
     */
    public Map<String, String> getAspects() {
        return Collections.unmodifiableMap(aspect);
    }

    /**
     * アスペクト設定判定.
     * @return true=アスペクトあり
     */
    public boolean isAspect() {
        return !aspect.isEmpty();
    }
// =================================================

    /**
     * ダンプを出力する。<br />
     * @return ダンプ文字列
     */
    @java.lang.Override
    public String toString() {

        // デバッグ用途なので定数参照しない
        final String lf = System.getProperty("line.separator");
        final StringBuilder sb = new StringBuilder()
            .append("<component id='").append(beanID)
            .append("' create='").append(createTypes.getLabel())
            .append("' type='").append(typeTypes.getLabel())
            .append("' src='").append(src)
            .append("'>").append(lf);

        if (!injection.isEmpty()) {
            sb.append(TAB).append("<injection>").append(lf);
            for (final Map.Entry<String, String> entry : injection.entrySet())
                sb.append(TAB).append(TAB).append("<method name='").append(entry.getKey()).append("' id='").append(entry.getValue()).append("'/>").append(lf);
            sb.append(TAB).append("</injection>").append(lf);
        }

        if (!aspect.isEmpty()) {
            sb.append(TAB).append("<aspect>").append(lf);
            for (final Map.Entry<String, String> entry : aspect.entrySet())
                sb.append(TAB).append(TAB).append("<method name='").append(entry.getKey()).append("' id='").append(entry.getValue()).append("'/>").append(lf);
            sb.append(TAB).append("</aspect>").append(lf);
        }

        sb.append("</component>").append(lf);

        return sb.toString();
    }
}
