/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.container;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jp.gr.java_conf.fragile.commons.aspect.AspectHander;
import jp.gr.java_conf.fragile.commons.aspect.Interceptor;
import jp.gr.java_conf.fragile.commons.collect.Collect;
import jp.gr.java_conf.fragile.commons.container.config.BeanMetaData;
import jp.gr.java_conf.fragile.commons.container.config.CreateTypes;
import jp.gr.java_conf.fragile.commons.container.config.TypeTypes;
import jp.gr.java_conf.fragile.commons.container.config.XMLProperties;
import jp.gr.java_conf.fragile.commons.exceptions.FGRuntimeException;
import jp.gr.java_conf.fragile.commons.reflect.DynamicBeanFactory;

/**
 * 簡易DIコンテナ.<br />
 * @param <T> 生成する型
 */
public class ComponentFactory<T> {

    /** コンテキストはキャッシュする */
    private static Context context = null;

    /** シングルトンキャッシュ */
    private static final Map<String, Object> singleton = Collections.synchronizedMap(new HashMap<String, Object>());

// =========================================================

    /** 返却クラス */
    private Class<T> clazz = null;

    /** プロパティを取得する */
    private XMLProperties properties = null;

    /**
     * トランザクション単位にJNDIのlookupはキャッシュする。<br />
     * ※ トランザクション単位なので、インスタンスフィールドとなります<br />
     */
    private Map<String, Object> jndiMap = Collect.newHashMap();

// =========================================================

    /**
     * コンストラクタ. 外部生成禁止
     * @param clazz クラス
     * @param xmlProperties プロパティクラス
     */
    protected ComponentFactory(final Class<T> clazz, final XMLProperties xmlProperties) {
        this.clazz = clazz;
        this.properties = xmlProperties;
    }

// =========================================================

    /**
     * ファクトリのファクトリ.
     *
     * <pre>
     * ファクトリのインスタンスを生成し初期化します
     * InitialContextに関しては初回のみ生成でキャッシュを行います
     * 後でファクトリの入れ替えを容易にする為、
     * 設計時点からファクトリの生成を外部にします
     * ※ 業務処理からのnewは極力排除します
     * </pre>
     * @param <T> 生成する型
     * @param clazz 取得するBeanの型(インターフェース型を設定する事)
     * @return ファクトリのインスタンス
     */
    public static <T> ComponentFactory<T> createFactory(final Class<T> clazz) {

        if (!clazz.isInterface()) {
            // アスペクト適用時の引数はInterfaceを指定してください。{0}
            throw new FGRuntimeException("Not Interface:" + clazz);
        }

        final ComponentFactory<T> instance = new ComponentFactory<T>(clazz, XMLProperties.getInstance());

        if (context == null) {
            try {
                context = new InitialContext();
            } catch (NamingException e) {
                // InitialContextの生成に失敗しました
                throw new FGRuntimeException(e);
            }
        }

        return instance;
    }

// =========================================================

    /**
     * コンポーネントを取得.
     * @param name コンポーネント名
     * @return インスタンス
     */
    public T getComponent(final ComponentNameable name) {

        @SuppressWarnings("deprecation")
        final T component = getComponent(name.toString());

        return component;
    }

    /**
     * コンポーネントを取得.
     *
     * <pre>
     * ComponentNameableの引数メソッドを使用してください
     * ※ JUnit試験用途にpublicにします
     * </pre>
     * @param beanID beanのID
     * @return インスタンス
     */
    @Deprecated
    public T getComponent(final String beanID) {

        final Object bean = loadComponent(beanID);

        return clazz.cast(bean);
    }

    /**
     * コンポーネントのロード処理.
     *
     * <pre>
     * 再起処理しています。
     * 循環参照した場合はスタックオーバーフローで落ちます
     * ※ 循環参照チェックは実装しません
     * インジェクションに処理について<br />
     * <code>
     *  ComponentFactory&lt;Foo&gt; factory
     *     = ComponentFactory.createFactory(Foo.class);
     *  Foo bean = factory.getComponent("FooBean");
     * </code>
     * </pre>
     * @param beanID ロードするコンポーネント
     * @return 指定したコンポーネントのインスタンス
     */
    protected Object loadComponent(final String beanID) {

        // 定義情報の取得
        final BeanMetaData beanMetaData = getBeanMetaData(beanID);

        // 定義されたクラスのインスタンスを取得
        Object bean;
        try {
            bean = getComponentInstance(beanMetaData);
        } catch (Exception e) {
            throw new FGRuntimeException("loadComponent error beanID:" + beanID, e);
        }

        // インスタンスに対してsetterインジェクション
        doDISetters(beanMetaData.getInjections(), bean);

        // アスペクトを設定してインスタンスを入れ替える(DIした後で行わないとだめ)
        if (!beanMetaData.isAspect()) {
            return bean;
        }

        // インターフェースの場合のみアスペクト定義できる
        if (!clazz.isInterface())
            throw new FGRuntimeException("Aspect Not Interface:" + beanID);

        // ラップする
        bean = DynamicBeanFactory.proxyWrap(bean, createAspectHander(beanMetaData, bean));

        return bean;
    }

    /**
     * アスペクトハンドラーを生成する.
     * @param beanMetaData Bean定義
     * @param bean インスタンス
     * @return ハンドラー
     */
    protected AspectHander createAspectHander(final BeanMetaData beanMetaData, final Object bean) {
        final AspectHander aspectHander = new AspectHander(bean);
        // インスペクターの取得
        for (final Map.Entry<String, String> aspect : beanMetaData.getAspects().entrySet()) {
            final String interceptMethod = aspect.getKey();
            for (final String intercepterID : aspect.getValue().split(",")) {
                final Object interceptor = loadComponent(intercepterID);
                if (!(interceptor instanceof Interceptor)) {
                    // インスペクターではありません
                    throw new FGRuntimeException("Not Interceptor:" + intercepterID);
                }
                aspectHander.addInterceptor(interceptMethod, Interceptor.class.cast(interceptor));
            }
        }
        return aspectHander;
    }

    /**
     * Bean定義情報を取得する.
     * getBeanMetaDataを実行する
     * @param beanID BeanID
     * @return メタ情報
     */
    protected BeanMetaData getBeanMetaData(final String beanID) {

        if (beanID == null || beanID.length() == 0) {
            // BeanIDが不正です id[{0}]
            throw new FGRuntimeException("Missing beanID:" + beanID);
        }

        final BeanMetaData beanMetaData = properties.getBeanMetaData(beanID);

        if (beanMetaData == null) {
            // Bean定義がありません id[{0}]
            throw new FGRuntimeException("Missing beanID:" + beanID);
        }

        return beanMetaData;
    }

    /**
     * ロードタイプを判定し、インスタンス生成を振り分ける。<br />
     * @param beanMetaData ソース(クラス名 or jndi名)
     * @return インスタンス
     */
    protected Object getComponentInstance(final BeanMetaData beanMetaData) {

        Object component = null;
        final CreateTypes createTypes = beanMetaData.getCreate();
        final String beanID = beanMetaData.getBeanID();

        synchronized (ComponentFactory.class) {
            // キャッシュから取得する
            if (createTypes.isCache() && singleton.containsKey(beanID))
                return singleton.get(beanID);

            // インスタンスを取得する
            component = loadInstance(beanMetaData);

            // キャッシュ設定
            if (createTypes.isCache())
                singleton.put(beanID, component); // nullも含む

            // MBEAN登録
            if (createTypes == CreateTypes.MBEAN) {
                registerMBean(component, createMBeanName(component));
            }
        }
        return component;
    }

    /**
     * ロード方式のディスパッチ.
     * @param src ソース
     * @param load 方法
     * @return インスタンス
     */
    protected Object loadInstance(final BeanMetaData beanMetaData) {

        Object component = null;

        final TypeTypes type = beanMetaData.getType();
        final CreateTypes createTypes = beanMetaData.getCreate();
        final String src = beanMetaData.getSrc();

        switch (type) {
        // BeanとDAOは同じとする
        case BEAN:
        case DAO:
            // lookupして終わり
            if (createTypes == CreateTypes.JNDI) {
                component = lookupInstance(src);
                break;
            }
            component = createInstance(src);
            break;
        case BOOLEAN:
            component = Boolean.valueOf(src);
            break;
        case CHAR:
            if (src != null && src.length() > 1) {
                component = Character.valueOf(src.charAt(0));
            } else {
                final char ch = 0x00;
                component = Character.valueOf(ch);
            }
            break;
        case INT:
            component = Integer.valueOf(src);
            break;
        case LONG:
            component = Long.valueOf(src);
            break;
        case STAING:
            if (!BeanMetaData.EMPTY_STRING.equals(src)) {
                component = src;
            }
            break;
        default:
            // Beanロードに失敗しました src[{0}] load[{1}]
            throw new FGRuntimeException("loadInstance Error:" + beanMetaData.getBeanID());
        }

        return component;
    }

    /**
     * インスタンスを生成する.
     * @param clzName クラス文字列
     * @return インスタンス
     */
    protected Object createInstance(final String clzName) {

        Object instance = null;
        try {
            instance = DynamicBeanFactory.create(clzName, Object.class);
        } catch (Exception e) {
            // インスタンス生成に失敗しました name[{0}]
            throw new FGRuntimeException("createInstance Error:" + clzName, e);
        }
        return instance;
    }

    /**
     * インスタンスを取得.
     *
     * <pre>
     * トランザクション単位でキャッシュします。
     * 一度lookupしたObjectはMapに保持し同じインスタンスを戻します。
     * DB関連のリソースを前提としてます。
     * </pre>
     * @param jndiPath JNDIパス
     * @return インスタンス
     */
    protected Object lookupInstance(final String jndiPath) {

        Object instance = jndiMap.get(jndiPath);
        if (instance != null)
            return instance;

        try {
            instance = context.lookup(jndiPath);
            jndiMap.put(jndiPath, instance);
        } catch (NamingException e) {
            // JNDIからの取得に失敗しました path[{0}]
            throw new FGRuntimeException("context.lookup error:" + jndiPath, e);
        }
        return instance;
    }

    /**
     * MBeanサーバへ登録. PlatformMBeanServerに登録する.
     * @param mbean インスタンス
     * @param name 登録名
     */
    protected void registerMBean(final Object mbean, final String name) {

        if (mbean == null)
            return;
        try {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(mbean, new ObjectName(name));
        } catch (Exception e) {
            // MBeanサーバへの登録に失敗しました。[{0}] [{1}] [{2}]
            throw new FGRuntimeException("registerMBean error:" + name, e);
        }
    }

    /**
     * MBeanの名称を生成する.
     * @param mbean 　登録するインスタンス
     * @return 生成した名称
     */
    protected String createMBeanName(final Object mbean) {

        final Class<?> mbeanclazz = mbean.getClass();
        final Package pac = mbeanclazz.getPackage();
        final String pacName = pac.getName();
        String clazzName = mbeanclazz.getName();
        final int idx = clazzName.lastIndexOf('.');
        if (idx != -1) {
            clazzName = clazzName.substring(idx + 1);
        }

        return pacName + ":type=" + clazzName;
    }

    /**
     * セッター定義単位にコンポーネントをロードしてセットする.
     * @param setters セットするコンポーネント定義コレクション
     * @param bean セットするインスタンス
     */
    protected void doDISetters(final Map<String, String> injections, final Object bean) {

        for (final Map.Entry<String, String> entry : injections.entrySet()) {

            final String inMethod = entry.getKey();
            final String inBeanID = entry.getValue();
            if (inBeanID == null || inMethod == null || inBeanID.length() == 0 || inMethod.length() == 0)
                // Bean定義が不正です id[{0}] Method[{1}]
                throw new FGRuntimeException("injections def error:" + inBeanID + ":" + inMethod + ":" + bean);

            // 再起ロードしてコンポーネントをセットする
            for (final String injectionBeanID : inBeanID.split(","))
                doDISetter(bean, inMethod, loadComponent(injectionBeanID.trim()));
        }
    }

    /**
     * 指定メソッドにObjectを設定する.
     * @param bean 設定されるインスタンス
     * @param inMethodName セッターメソッド名
     * @param setObj セットするインスタンス
     */
    protected void doDISetter(final Object bean, final String inMethodName, final Object setObj) {

        try {
            // パラメータ型は不明の為、配列取得を行う
            for (final Method method : bean.getClass().getMethods()) {
                if (!inMethodName.equals(method.getName()))
                    continue;
                DynamicBeanFactory.setAccessible(method);
                method.invoke(bean, setObj);
                break;
            }
        } catch (Exception e) {
            // セッター登録に失敗しました MethodName[{0}] bean[{1}]
            throw new FGRuntimeException("doDISetter error:" + inMethodName + ":" + bean, e);
        }
    }
}
