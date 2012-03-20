/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net.pool;

import java.net.Socket;

import jp.gr.java_conf.fragile.service.net.AbstractListenerCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * アプリスレッドグループ.
 */
class ServerSocketPoolThreadGroup extends ThreadGroup {

    /** ロガー */
    private static Logger log = LoggerFactory.getLogger(ServerSocketPoolThreadGroup.class);

// =======================================================================

    /**
     * コンストラクタ.
     * @param name スレッドグループ名
     */
    public ServerSocketPoolThreadGroup(final String name) {
        super(name);
    }

    /**
     * アプリ障害発生時に実行される.
     * <pre>
     * </pre>
     * @param thread スレッド
     * @param thr 例外
     */
    @java.lang.Override
    public void uncaughtException(final Thread thread, final Throwable thr) {

        if (log.isTraceEnabled()) {
            log.trace("START", thr);
        }

        try {

            if (!(thread instanceof ServerSocketPoolThread)) {
                // スレッドクラスのインスタンスが不正です [{0}]
                log.warn("not ServerSocketPoolThread");
                return;
            }

            final ServerSocketPoolThread sspth = ServerSocketPoolThread.class.cast(thread);
            final AbstractListenerCommand lc = sspth.getListenerCommand();

            if (lc == null) {
                log.error("APP error");
                return;
            }

            final Socket socket = lc.getSocket();
            // ip = manager.getIPAddress();
            // アプリスレッドで例外が発生しました IP[{0}]
            log.error("APP error", thr);

            if (!socket.isClosed()) {
                // 基本的に例外は発生しない
                socket.close();
            }

        } catch (Throwable e) {
            // アプリスレッド例外処理で例外が発生しました IP[{0}]
            log.error("error handring error", e);
            if (e instanceof Error) {
                throw Error.class.cast(e);
            }
        }
    }
}
