/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.container.config;

import java.util.Collections;
import java.util.Map;

import jp.gr.java_conf.fragile.commons.collect.Collect;
import jp.gr.java_conf.fragile.commons.exceptions.FGRuntimeException;

/**
 * XML定義値オブジェクト.<br />
 */
public final class XMLProperties {

    /** シングルトン */
    private static XMLProperties instance;

    /** 保持する */
    private static final XMLReader reader = new XMLReader();

// =============================================================================

    /** Beanの定義情報コレクション */
    private Map<String, BeanMetaData> beanMetaDatas = Collect.newHashMap();

    /**
     * コンストラクタ。<br />
     */
    private XMLProperties() {
    }

// =============================================================================

    /**
     * 初期化する.<br />
     * MBeanから実行する
     */
    public static void init() {
        instance = null;
    }

    /**
     * シングルトン取得。<br />
     * <pre>
     * インスタンスが存在しない場合はXMLReaderで値を設定する。
     * サービス起動のmainスレッドで読み込むので特に同期化は行わない
     * </pre>
     * @return インスタンス
     */
    public static XMLProperties getInstance() {

        if (instance != null)
            return instance;

        final XMLProperties properties = new XMLProperties();

        try {
            reader.read(properties);
        } catch (Exception e) {
            // XML定義読み出しに失敗しました
            throw new FGRuntimeException("XMLReader error", e);
        }

        if (instance == null) // 同期化してないので保険
            instance = properties;

        return instance;
    }

//  =========================================================================

    /**
     * Bean定義情報を戻す.<br />
     * @param beanID BeanID
     * @return Bean定義情報
     */
    public BeanMetaData getBeanMetaData(final String beanID) {
        return beanMetaDatas.get(beanID);
    }

    /**
     * Bean定義情報を設定する.<br />
     * @param beanID BeanID
     * @param beanMetaData Bean定義情報
     */
    public void putBeanMetaData(final String beanID, final BeanMetaData beanMetaData) {
        if (beanMetaDatas.put(beanID, beanMetaData) != null) {
            // BeanIDが重複しています。{0}
            throw new FGRuntimeException(beanID + "dupe");
        }
    }

    /**
     * 不変マップにして戻す.
     * @return マップ
     */
    protected Map<String, BeanMetaData> getBeanMetaDatas() {
        return Collections.unmodifiableMap(beanMetaDatas);
    }
}
