/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net.pool;

import java.util.concurrent.ThreadFactory;

/**
 * ThreadPoolExecutorのコールバック.
 */
class ServerSocketPoolThreadFactoryImpl implements ThreadFactory {

    /** スレッドグループ */
    private ThreadGroup threadGroup = null;

//  =======================================================================

    /**
     * コンストラクタ。<br />
     */
    private ServerSocketPoolThreadFactoryImpl() {
    }

    /**
     * コンストラクタ.
     * <pre>
     * スレッドグループをフィールドに保持する
     * </pre>
     * @param threadGroup スレッドグループ
     */
    public ServerSocketPoolThreadFactoryImpl(final ThreadGroup threadGroup) {
        this();
        this.threadGroup = threadGroup;
    }

    /**
     * 拡張スレッドクラスのインスタンスを戻す.
     * <pre>
     * </pre>
     * @param runnable 実行クラス
     * @return スレッド
     */
    public ServerSocketPoolThread newThread(final Runnable runnable) {
        final ServerSocketPoolThread thread = new ServerSocketPoolThread(threadGroup, runnable);
        return thread;
    }
}
