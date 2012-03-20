/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.aspect;

/**
 * メソッドインターセプター
 */
public interface Interceptor {

    /**
     * 前処理.
     * @param instance 実行するクラスのインスタンス
     * @param param メソッドの引数
     * @return このメソッドの引数(通常はそのままparamを戻してください)
     */
    Object[] before(final Object instance, final Object[] param);

    /**
     * 後処理.
     * @param instance 実行するクラスのインスタンス
     * @param returnValue メソッド戻り値
     * @return このメソッドの戻り値(通常はそのままreturnValueを戻してください)
     */
    Object after(final Object instance, final Object returnValue);
}
