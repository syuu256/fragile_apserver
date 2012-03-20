/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net;

import java.net.Socket;
import java.util.Set;

import jp.gr.java_conf.fragile.commons.exceptions.FGRuntimeException;
import jp.gr.java_conf.fragile.service.net.pool.ServerSocketPoolThread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 受信時のコマンド. <br />
 */
public abstract class AbstractListenerCommand implements Runnable, CommandExecutionable {

    /** ロガー */
    private static Logger log = LoggerFactory.getLogger(AbstractListenerCommand.class);

// =============================================================================

    /** ソケット */
    private Socket socket = null;

    /** スレッド開始時間 */
    private long startTime = 0L;

    /** 実行中のスレッド */
    private Thread runThread = null;

    /** タイムアウトした場合にtrueにする */
    private boolean isTimeout = false;

    /** 現在実行中のスレッド */
    private Set<CommandExecutionable> executes;

// =============================================================================

    /**
     * ソケットを戻す.
     * @return ソケット
     */
    public final Socket getSocket() {
        return socket;
    }

    /**
     * ソケットを設定する.
     * @param socket ソケット
     */
    public final void setSocket(final Socket socket) {
        this.socket = socket;
    }

    /**
     * スレッドを開始した時間を戻す.
     * <pre>
     * スレッドフリーズまたは
     * 処理タイムアウト制御に使用
     * </pre>
     * @return 開始ミリ秒
     */
    public final long getStartTime() {
        return startTime;
    }

    /**
     * 現在実行中スレッド管理.
     * @param executes
     */
    public final void setExecutes(final Set<CommandExecutionable> executes) {
        this.executes = executes;
    }

    /**
     * 強制終了する場合に割り込みを上げる.
     */
    public final void interrupt() {

        // 開始していない
        if (runThread == null)
            return;

        // 自スレッドよりの呼び出しは却下
        if (runThread == Thread.currentThread())
            return;

        // 割り込みを上げる
        synchronized (AbstractListenerCommand.class) {
            runThread.interrupt();
        }

        isTimeout = true;
    }

// =============================================================================

    /**
     * 実行中スレッド監視制御用.
     */
    public final void run() {

        // 開始時間を設定
        startTime = System.currentTimeMillis();

        if (log.isTraceEnabled()) {
            log.trace("start:" + startTime);
        }

        ServerSocketPoolThread serverSocketPoolThread = null;

        try {

            // 外部割り込み用
            runThread = Thread.currentThread();
            serverSocketPoolThread = ServerSocketPoolThread.class.cast(runThread);
            serverSocketPoolThread.setListenerCommand(this);

            executes.add(this);

            // 処理実行
            execute();

            // Threadは再利用されるはずなので初期化
            serverSocketPoolThread.setListenerCommand(null);

            if (log.isTraceEnabled()) {
                log.trace("end:" + (System.currentTimeMillis() - startTime));
            }

            startTime = 0L;

        } catch (Exception e) {

            if (isTimeout) {
                throw new CommandTimeoutException(e);
            }

            if (e instanceof RuntimeException) {
                throw RuntimeException.class.cast(e);
            }

            throw new FGRuntimeException(e);
        } finally {
            // 再利用されるので初期化する
            runThread = null;
            if (serverSocketPoolThread != null) {
                serverSocketPoolThread.setListenerCommand(null);
            }
            executes.remove(this);
        }
    }


// =============================================================================

    /**
     * スレッド処理.
     */
    public abstract void execute();

    /**
     * スレッド生成拒否.
     */
    public abstract void abort();

}
