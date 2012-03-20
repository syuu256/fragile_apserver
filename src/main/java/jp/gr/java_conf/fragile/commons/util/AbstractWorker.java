/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import jp.gr.java_conf.fragile.commons.exceptions.FGRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 常駐スレッドの抽象クラス.
 */
public abstract class AbstractWorker implements Runnable {

    /** ロガー */
    private static Logger log = LoggerFactory.getLogger(AbstractWorker.class);

    /** サーバ停止フラグ trueの場合は停止 */
    private AtomicBoolean stop = new AtomicBoolean(false);

    /** sleep開始時間. */
    private AtomicLong startTime = new AtomicLong(0L);

    /**
     * コンストラクタ
     */
    public AbstractWorker() {
    }

    /**
     * スリープ開始時間.<br />
     * スレッドフリーズ監視をする場合に使用する
     * @return 開始時間
     */
    public final long getSleepStartTime() {

        return startTime.longValue();
    }

    /**
     * ループ終了判定。<br />
     * @return true = ループ終了
     */
    public final boolean isStop() {

        return stop.get();
    }

    /**
     * サーバソケットスレッドの停止を行う。<br />
     *
     * <pre>
     * 本メソッド呼び出し終了で、スレッドは終了しています。
     * 以下の処理を行っています
     * 1) closeにtrueを設定します。
     *    listenerスレッドで監視しています。
     * 2) 引数のスレッドに割込みを行う。
     * 3) 引数のスレッドでjoinを行い、スレッド停止を待つ
     *
     * ※ このメソッドが呼び出されるスレッドに対しての割込みは想定外です
     * </pre>
     * @param thread 停止要求するリスナースレッド
     */
    public final void stop(final Thread thread) {

        stop();

        // 割り込みをあげる
        thread.interrupt();

        // 停止を待つ
        try {
            thread.join();
        } catch (InterruptedException e) {
            // SocketListener停止待ち中に割込みが発生しました
            log.error(".SRM10180W", e);
            throw new FGRuntimeException(e);
        }
    }

    /**
     * 停止フラグを上げる
     */
    protected final void stop() {

        stop.set(true);
    }

    /**
     * スレッドのエントリ。<br />
     *
     * <pre>
     * listener();を実行する
     * 上記メソッドは停止指示が無い限りループするので戻ってこない
     * 停止された場合は、フィールドの参照を消してGCの回収させる
     * ※ 例外発生時もnullを代入する事
     * ※ Runnableのエントリとは
     *    別に処理メソッドを記述するようにしています
     * ※ finally節でソケットサーバをクローズします
     *    ServerSocket#closeの例外はListenerUncaughtExceptionHandler
     *    で正常に処理する事
     *
     * </pre>
     */
    public final void run() {

        if (log.isTraceEnabled()) {
            log.trace("START");
        }

        try {
            startUp();
            worker();
        } finally {
            shutdown();
        }

        if (log.isTraceEnabled()) {
            log.trace("END");
        }
    }

    /**
     * ループ処理
     */
    private final void worker() {

        if (log.isTraceEnabled()) {
            log.trace("START");
        }

        stop.set(false);

        while (!isStop()) {

            startTime.set(System.currentTimeMillis());

            work(); // 処理呼び出し

            if (log.isTraceEnabled()) {
                final long end = System.currentTimeMillis();
                final long time = end - startTime.longValue();
                log.trace("time:" + String.valueOf(time));
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("END");
        }
    }

// =============================================================================

    /**
     * スレッドfinallyの実装を行う.<br />
     *
     * <pre>
     * 常住の資源解放など
     * </pre>
     */
    protected void shutdown() {
        return;
    }

    /**
     * スレッドの開始直後の処理を実装する
     */
    protected void startUp() {
        return;
    }

    /**
     * ループ内処理を実装する.<br />
     *
     * <pre>
     * スリープは各自実装する事
     * continueはreturn
     * berakはstopを実行してください
     * </pre>
     */
    protected abstract void work();
}
