/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net.pool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import jp.gr.java_conf.fragile.service.net.AbstractListenerCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * スレッドプールの受付不可時のコールバック.
 * <pre>
 *  ※ シャットダウンでキュー受け入れ不可の場合に接続を切る
 *     必要があるので実装しました
 *  ※ キャパシティオーバー、キュー待機時間オーバーの時なども
 *     呼び出されます
 * </pre>
 */
class RejectedPolicy implements RejectedExecutionHandler {

    /** ロガー */
    private static Logger log = LoggerFactory.getLogger(RejectedPolicy.class);

// =======================================================================

    /**
     * スレッドプールの受付不可時、実行される.
     * @param run 実行するクラス
     * @param executor プール
     */
    public void rejectedExecution(final Runnable run, final ThreadPoolExecutor executor) {

        if (log.isTraceEnabled()) {
            log.trace("START");
        }

        try {

            if (!(run instanceof AbstractListenerCommand)) {
                // キューのインスタンスが不正です [{0}]
                log.warn("AbstractListenerCommand not extends");
                return;
            }
            final AbstractListenerCommand command = AbstractListenerCommand.class.cast(run);
            command.abort();

        } catch (Throwable e) {
            // 受付不可処理で例外が発生しました IP[{0}]
            log.error("rejectedExecution", e);
            if (e instanceof Error) {
                throw Error.class.cast(e);
            }
        }
    }
}
