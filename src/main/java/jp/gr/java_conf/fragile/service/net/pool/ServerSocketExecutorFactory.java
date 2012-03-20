/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net.pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * スレッドプールのクラスを戻す.
 * <pre>
 * Abstract Factoryにして生成方法を分離する
 * スレッドプールの引数等のチューニングが行いえるように
 * 実装クラスの生成方法を分離し依存関係を断ち切ります
 * </pre>
 */
public class ServerSocketExecutorFactory {

    /** ロガー */
    private static Logger log = LoggerFactory.getLogger(ServerSocketExecutorFactory.class);

    /** スレッドグループ名 */
    private static final String THREAD_GROUP_NAME = "ServerSocketPoolThreadGroup";

//  ============================================================================

    /** スレッドグループ */
    private ThreadGroup threadGroup = new ServerSocketPoolThreadGroup(THREAD_GROUP_NAME);

    /** リジェクト */
    private RejectedExecutionHandler rejectedExecutionHandler = new RejectedPolicy();

    /** 常に処理待ちのスレッド数. */
    private int executorCorePoolSize = 10;

    /** 最大負荷時の平行稼動数. */
    private int executorMaximumPoolSize = 20;

    /** poolから破棄するまでの時間. */
    private long executorKeepAliveTime = 10000L;

    /** キューの容量(待ち行列数). */
    private int executorPoolCapacity = 60;

//  ============================================================================

    /**
     * ThreadPoolExecutorの生成.
     * <pre>
     * </pre>
     * @return 生成して戻す (共変戻り値)
     */
    public final ThreadPoolExecutor createExecutor() {

        if (log.isTraceEnabled()) {
            log.trace("START");
        }

        final int capacity = executorPoolCapacity;
        final int corePoolSize = executorCorePoolSize;
        final int maximumPoolSize = executorMaximumPoolSize;
        final long keepAliveTime = executorKeepAliveTime;

        if (log.isDebugEnabled()) {
            log.debug("capacity[" + capacity + "] ");
            log.debug("corePoolSize[" + corePoolSize + "] ");
            log.debug("maximumPoolSize[" + maximumPoolSize + "] ");
            log.debug("keepAliveTime[" + keepAliveTime + "] ");
        }

        ThreadPoolExecutor executor = null;

        try {

            executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                getTimeUnit(),
                createQueue(capacity),
                createThreadFactory(getThreadGroup()),
                createRejectedExecutionHandler());

        } catch (Exception e) {
            // ThreadPoolExecutorの生成に失敗しました
            // capacity[{0}] corePoolSize[{1}]
            // maximumPoolSize[{2}] keepAliveTime[{3}]
            log.error("ThreadPoolExecutor create error", e);

            throw new RuntimeException(e);
        }

        return executor;
    }

    /**
     * スレッドグループをを設定.
     * @param threadGroup スレッドグループ
     */
    public final void setThreadGroup(final ThreadGroup threadGroup) {
        this.threadGroup = threadGroup;
    }

    /**
     * リジェクトハンドリングを設定する.
     * @param rejectedExecutionHandler リジェクトハンドラ
     */
    public final void setRejectedExecutionHandler(final RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }

    /**
     * キューの数.
     * @param executorPoolCapacity リジェクト閾値
     */
    public final void setExecutorPoolCapacity(final int executorPoolCapacity) {
        this.executorPoolCapacity = executorPoolCapacity;
    }

    /**
     * スレッドインスタンスを保持する数.
     * @param executorCorePoolSize プールサイズ
     */
    public final void setExecutorCorePoolSize(final int executorCorePoolSize) {
        this.executorCorePoolSize = executorCorePoolSize;
    }

    /**
     * 最大同時実行数.
     * @param executorMaximumPoolSize 同時実行数
     */
    public final void setExecutorMaximumPoolSize(final int executorMaximumPoolSize) {
        this.executorMaximumPoolSize = executorMaximumPoolSize;
    }

    /**
     * キュー待ちタイムアウト値
     * @param executorKeepAliveTime タイムアウト
     */
    public final void setExecutorKeepAliveTime(final long executorKeepAliveTime) {
        this.executorKeepAliveTime = executorKeepAliveTime;
    }

    /**
     * スレッドグループを取得する.
     * @return threadGroup スレッドグループ
     */
    public final ThreadGroup getThreadGroup() {
        return threadGroup;
    }

//  ============================================================================
// オーバーライド可
//  ============================================================================

    /**
     * キューの実装を戻す.
     * @param capacity キューのキャパシティ
     * @return インスタンス
     */
    protected BlockingQueue<Runnable> createQueue(final int capacity) {
        return new ArrayBlockingQueue<Runnable>(capacity);
    }

    /**
     * スレッド生成の実装を戻す.
     * @param tg インスタンス
     */
    protected ThreadFactory createThreadFactory(final ThreadGroup tg) {
        return new ServerSocketPoolThreadFactoryImpl(tg);
    }

    /**
     * RejectedExecutionHandlerのインスタンスを戻す.
     * @return インスタンス
     */
    protected RejectedExecutionHandler createRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    /**
     * TimeUnitのインスタンスを戻す.
     * @return インスタンス
     */
    protected TimeUnit getTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }
}
