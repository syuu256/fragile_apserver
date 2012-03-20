/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.container.config;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jp.gr.java_conf.fragile.commons.exceptions.FGRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML定義値読み出しクラス.<br />
 */
class XMLReader {

    /** ログ出力クラス定義 */
    private static Logger log = LoggerFactory.getLogger(XMLReader.class.getName());

    /** -Dのキー名 */
    public static final String CONFIG_PSTH_KEY = "jp.gr.java_conf.fragile.commons.container.config";

    /** XMLファイルパス */
    public static final String FILE_PATH = "/component.xml";

    /** Xpath */
    protected static final String COMPONENT_XPATH = "/container/component";

    /** タグ名 */
    protected static final String TAG_INJECTION = "injection";

    /** タグ名 */
    protected static final String TAG_ASPECT = "aspect";

    /** タグ名 */
    protected static final String TAG_METHOD = "method";

    /** 属性名 */
    protected static final String ATTR_BEANID = "id";

    /** 属性名 */
    protected static final String ATTR_CREATE = "create";

    /** 属性名 */
    protected static final String ATTR_TYPE = "type";

    /** 属性名 */
    protected static final String ATTR_SRC = "src";

    /** 属性名 */
    protected static final String ATTR_METHOD_ID = "id";

    /** 属性名 */
    protected static final String ATTR_METHOD_NAME = "name";

    /** 現在参照中のComnfigパス */
    private static String configPath = null;

//  =========================================================================

    /** XPath */
    private XPath xpath = null;

    /** ドキュメント */
    private Document root = null;

//  =========================================================================

    /**
     * コンフィグパス.<br />
     * @return コンフィグパス
     */
    public static String getConfigPath() {
        return XMLReader.configPath;
    }

    /**
     * コンフィグパス.<br />
     * @param configPath コンフィグパス
     */
    public static void setConfigPath(final String configPath) {
        XMLReader.configPath = configPath;
    }

//  =========================================================================

    /**
     * XMLを読み込む.
     * <pre>
     * パラメータのXMLPropertiesに設定する。
     * </pre>
     * @param xmlProperties 値オブジェクト
     */
    public void read(final XMLProperties xmlProperties) {

        root = getRootDocument();
        xpath = XPathFactory.newInstance().newXPath();

        try {
            loadContainer(xmlProperties); // コンポーネントの取得
        } catch (XPathExpressionException e) {
            // XPathの実行に失敗しました
            throw new FGRuntimeException(e);
        }
    }

    /**
     * コンテナタグ情報を取得する.
     * @param xmlProperties 設定する値オブジェクト
     * @throws XPathExpressionException XPath例外
     */
    protected void loadContainer(final XMLProperties xmlProperties) throws XPathExpressionException {

        final Object o = xpath.evaluate(COMPONENT_XPATH, root, XPathConstants.NODESET);
        final NodeList components = NodeList.class.cast(o);

        for (int i = 0; i < components.getLength(); i++) {

            final Element component = Element.class.cast(components.item(i));
            final BeanMetaData beanMetaData = new BeanMetaData()
                .setBeanID(component.getAttribute(ATTR_BEANID))
                .setCreate(component.getAttribute(ATTR_CREATE))
                .setSrc(component.getAttribute(ATTR_SRC))
                .setType(component.getAttribute(ATTR_TYPE));

            loadContainerMethods(component, beanMetaData, TAG_INJECTION);

            loadContainerMethods(component, beanMetaData, TAG_ASPECT);

            xmlProperties.putBeanMetaData(beanMetaData.getBeanID(), beanMetaData);
        }
    }

    /**
     * セッター情報を解析して設定する.
     * @param component コンポーネントタグ
     * @param meta Bean定義情報
     * @param tag 解析するタグ
     * @throws XPathExpressionException XPath例外
     */
    protected BeanMetaData loadContainerMethods(final Element component, final BeanMetaData beanMetaData, final String tag) throws XPathExpressionException {

        final Object o = xpath.evaluate("./" + tag + "/" + TAG_METHOD, component, XPathConstants.NODESET);
        final NodeList methods = NodeList.class.cast(o);
        for (int j = 0; j < methods.getLength(); j++) {

            final Node n = methods.item(j);
            if (!(n instanceof Element))
                continue;

            final Element method = Element.class.cast(methods.item(j));
            if (!TAG_METHOD.equals(method.getTagName()))
                continue;

            final String name = method.getAttribute(ATTR_METHOD_NAME);
            final String id = method.getAttribute(ATTR_METHOD_ID);

            if (TAG_INJECTION.equals(tag))
                beanMetaData.putInjections(name, id);

            if (TAG_ASPECT.equals(tag))
                beanMetaData.putAspects(name, id);
        }

        return beanMetaData;
    }

    /**
     * XPathを実行する。<br />
     * @param str 式
     * @return 取得した文字列
     * @throws XPathExpressionException XPath例外
     */
    protected String eval(final String str) throws XPathExpressionException {

        final Object o = xpath.evaluate(str, root, XPathConstants.STRING);

        return String.class.cast(o);
    }

    /**
     * ドキュメントの生成。<br />
     * <pre>
     * DOMを使用しドキュメントを生成し戻す
     * </pre>
     * @return ドキュメント
     */
    protected Document getRootDocument() {

        Document document = null;
        InputStream inputStream = null;

        try {
            final DocumentBuilderFactory documentBuilderFactory
                = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // MBean経由で設定されている場合は設定値を使用する
            if (configPath == null) {
                // 初回に初期化して設定値を設定する
                // -Dにパスが定義されている場合には指定値を使用する
                // 設定が無い場合は初期値を使用する
                configPath = System.getProperty(CONFIG_PSTH_KEY, FILE_PATH);
            }

            inputStream = getXmlInputStream(configPath);
            document = documentBuilder.parse(inputStream);

            closeStream(inputStream);
        } catch (Exception e) {
            closeStream(inputStream);
            // DOMの生成に失敗しました
            throw new FGRuntimeException(e);
        }

        return document;
    }

    /**
     * ストリームをクローズする。<br />
     * @param inputStream クロースするストリーム
     */
    protected void closeStream(final InputStream inputStream) {

        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            // 入力ファイルのクローズに失敗しました
            throw new FGRuntimeException(e);
        }
    }

    /**
     * XMLファイルを取得。<br />
     * @param path パス
     * @return ストリーム
     */
    protected InputStream getXmlInputStream(final String path) {

        InputStream ret = null;
        try {
            log.info("LoadComfigPath:" + path);
            ret = XMLReader.class.getResourceAsStream(path);
            if (ret == null)
                throw new FGRuntimeException(path);
        } catch (Exception e) {
            // 定義ファイルのopenに失敗しました path[{0}]
            throw new FGRuntimeException(path, e);
        }
        return ret;
    }
}
