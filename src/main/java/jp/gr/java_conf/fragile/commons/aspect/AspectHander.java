/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.aspect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.fragile.commons.collect.Collect;
import jp.gr.java_conf.fragile.commons.exceptions.FGRuntimeException;
import jp.gr.java_conf.fragile.commons.reflect.DynamicBeanFactory;

/**
 * Hander
 */
public class AspectHander implements InvocationHandler {

    /** 本体 */
    private Object instance = null;

    /** 本体クラス */
    private Class<?> clazz = null;

    /**
     * コンストラクタ.
     * @param instance
     */
    public AspectHander(final Object instance) {
        this.instance = instance;
        this.clazz = instance.getClass();
    }

    /** インスペクターを保持 */
    private Map<String, List<Interceptor>> interceptors = Collect.newHashMap();

    /**
     * インスペクターを追加する.
     * @param methodName
     * @param interceptor
     * @return 自インスタンス
     */
    public AspectHander addInterceptor(final String methodName, final Interceptor interceptor) {

        List<Interceptor> list = interceptors.get(methodName);
        if (list == null) {
            list = Collect.newArrayList();
            interceptors.put(methodName, list);
        }
        list.add(interceptor);

        return this;
    }

    /**
     * メソッドを実行する.
     * @param proxy プロキシー
     * @param method メソッド
     * @param args 引数
     * @return 結果
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        final String methodName = method.getName();
        Object[] param = args;
        Method implMethod;
        try {
            implMethod = clazz.getDeclaredMethod(methodName, method.getParameterTypes());
            DynamicBeanFactory.setAccessible(implMethod);
        } catch (final NoSuchMethodException e) {
            throw new FGRuntimeException(e);
        }

        final List<Interceptor> interceptorList = interceptors.get(methodName);

        // 前処理
        param = before(interceptorList, param);

        Object returnValue;
        try {
            returnValue = implMethod.invoke(instance, param);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new FGRuntimeException(e);
        }

        // 後処理
        returnValue = after(interceptorList, returnValue);

        return returnValue;
    }

    /**
     * 前処理.
     * @param interceptorList インスペクター
     * @param args 引数
     * @return 引数値
     */
    private Object[] before(final List<Interceptor> interceptorList, final Object[] args) {

        if (interceptorList == null || interceptorList.isEmpty())
            return args;

        Object[] returnArg = args;
        for (final Interceptor interceptor : interceptorList)
            returnArg = interceptor.before(interceptor, returnArg);

        return returnArg;
    }

    /**
     * 後処理.
     * @param interceptorList インスペクター
     * @param returnValue メソッド戻り値
     * @return メソッド戻り値
     */
    private Object after(final List<Interceptor> interceptorList, final Object returnValue) {

        if (interceptorList == null || interceptorList.isEmpty())
            return returnValue;

        Object returnArg = returnValue;
        for (final Interceptor interceptor : interceptorList)
            returnArg = interceptor.after(interceptor, returnArg);

        return returnArg;
    }
}
