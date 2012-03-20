/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 処理スレッドを監視して
 * 規定時間以上の処理は停止要求をする
 * <pre>
 * ユーザー処理が何をするのか解らないので監視する
 * 自分の処理だけならば必要はない
 * </pre>
 */
class PoolThreadWatcher implements Runnable {

    /** ロガー */
    private static Logger log = LoggerFactory.getLogger(PoolThreadWatcher.class);

    /** 監視対象の親スレッド */
    private AbstractServerSocketListener listener = null;

    /** タイムアウト時間 */
    private long timeout = 0L;

    /**
     * コンストラクタ.
     * @param listener 監視対象
     * @param timeout 監視タイムアウト
     */
    public PoolThreadWatcher(final AbstractServerSocketListener listener, final long timeout) {
        this.listener = listener;
        this.timeout = timeout;
    }

    /**
     * 監視して停止する.
     */
    public void run() {

        if (log.isDebugEnabled()) {
            log.debug("start");
        }

        if (timeout == Long.MAX_VALUE) {
            if (log.isDebugEnabled()) {
                log.debug("監視しません");
            }
            return;
        }

        final Set<CommandExecutionable> commands = listener.getExecutesSnapShot();
        for (final CommandExecutionable ce : commands) {

            final long now = System.currentTimeMillis();
            final long start = ce.getStartTime();
            final long killTime = start + timeout;
            if (start == 0L) {
                // 監視対象外
                continue;
            }

            if (killTime >= now) {
                // 監視対象外
                continue;
            }

            // 割込を行う
            // Socket#closeは
            // ServerSocketPoolThreadGroup#uncaughtExceptionに任せる
            ce.interrupt();
        }

        if (log.isDebugEnabled()) {
            log.debug("end");
        }
    }
}
