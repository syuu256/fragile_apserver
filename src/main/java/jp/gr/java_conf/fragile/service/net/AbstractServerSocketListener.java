/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jp.gr.java_conf.fragile.commons.util.AbstractWorker;
import jp.gr.java_conf.fragile.service.net.pool.ServerSocketExecutorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 受信してスレッドを生成し
 * スレッドの中ではアクティブノードに追加する
 * 毎回作るので抽象化クラスにして再利用する
 */
public abstract class AbstractServerSocketListener extends AbstractWorker {

    /** ロガー */
    private static Logger log = LoggerFactory.getLogger(AbstractServerSocketListener.class);

// =======================================================================

    /** 実行キュー[上位より渡される] */
    private ThreadPoolExecutor threadPoolExecutor = null;

    /** ソケットサーバ [初期化で生成する]*/
    private ServerSocket serverSocket = null;

    /** 処理スレッド監視 */
    private ScheduledExecutorService watcherService = null;

    /** 処理スレッド監視 */
//    private ScheduledFuture<?> watcherServiceFuture = null;

    /** DIしてください */
    private ServerSocketExecutorFactory serverSocketExecutorFactory = null;

    /** スレッドに設定してください */
    private UncaughtExceptionHandler exceptionHandler = null;

    /** セレクター */
    private Selector serverSelector = null;

    /** 現在実行中のスレッド */
    private Set<CommandExecutionable> executes;

// =======================================================================

    /** バインドIPアドレス */
    private String bindAddress = null;

    /** バインドのポート */
    private int serverPort = 80;

    /** リスナーのバックログ */
    private int serverBackLog = 60;

    /** ブロックモードのタイムアウト */
    private int serverTimeout = 10000;

    /** ブロッキングモード */
    private boolean blocking = true;

    /** コマンドの実行タイムアウト */
    private long watcherCommandTimeout = Long.MAX_VALUE;

    /** コマンド監視待機時間 */
    private long watcherDelay = Long.MAX_VALUE;

//  =======================================================================

    /**
     * ファクトリを設定する.
     * @param factory ファクトリー
     */
    public final void setExecutorFactory(final ServerSocketExecutorFactory factory) {
        serverSocketExecutorFactory = factory;
    }

    /**
     * バインドIPアドレス に設定する.
     * @param bindAddress バインドIPアドレス
     */
    public final void setBindAddress(final String bindAddress) {
        this.bindAddress = bindAddress;
    }

    /**
     * バインドのポート に設定する.
     * @param serverPort バインドのポート
     */
    public final void setServerPort(final int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * リスナーのバックログ に設定する.
     * @param serverBackLog リスナーのバックログ
     */
    public final void setServerBackLog(final int serverBackLog) {
        this.serverBackLog = serverBackLog;
    }

    /**
     * ブロックモードのタイムアウト に設定する.
     * @param serverTimeout ブロックモードのタイムアウト
     */
    public final void setServerTimeout(final int serverTimeout) {
        this.serverTimeout = serverTimeout;
    }

    /**
     * ブロッキングモード に設定する.
     * @param blocking ブロッキングモード
     */
    public final void setBlocking(final boolean blocking) {
        this.blocking = blocking;
    }

    /**
     * コマンドの実行タイムアウト に設定する.
     * @param watcherCommandTimeout コマンドの実行タイムアウト
     */
    public final void setWatcherCommandTimeout(final long watcherCommandTimeout) {
        this.watcherCommandTimeout = watcherCommandTimeout;
    }

    /**
     * コマンド監視待機時間 に設定する.
     * @param watcherDelay コマンド監視待機時間
     */
    public final void setWatcherDelay(final long watcherDelay) {
        this.watcherDelay = watcherDelay;
    }

// =============================================================================

    /**
     * UncaughtExceptionHandler を取得する.
     * @return exceptionHandler
     */
    public final UncaughtExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * サーバソケットを生成する.
     */
    public final void initialize() {

        try {
            serverSelector = Selector.open();
        } catch (IOException e) {
            // Selector#openに失敗しました
            log.error("Selector#open error", e);
            throw new RuntimeException(e);
        }

        serverSocket = createServerSocket();

        threadPoolExecutor = serverSocketExecutorFactory.createExecutor();

        if (watcherDelay != -1) {
            // スレッド監視(監視スレッドの監視はしません)
            watcherService = Executors.newSingleThreadScheduledExecutor();
            final PoolThreadWatcher psw = new PoolThreadWatcher(this, watcherCommandTimeout);

//        watcherServiceFuture =
            watcherService.scheduleWithFixedDelay(psw, 0L,
                    watcherDelay, TimeUnit.MILLISECONDS);
        }

        exceptionHandler = new AbortExceptionHandler(this);

        final Set<CommandExecutionable> s = new HashSet<CommandExecutionable>();
        executes = Collections.synchronizedSet(s);
    }

    /**
     * サーバソケットを閉じる.
     * <pre>
     * ここで落ちた場合の想定が難しい
     * ListenerUncaughtExceptionHandlerの仕様に注意する事
     * ※ 緊急用途を考えて、publicにしておきます
     * </pre>
     */
    public final void closeServerSocket() {

        if (log.isTraceEnabled()) {
            log.trace("START");
        }

        try {
            // isClosed = ソケットが閉じた場合は true
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSelector.close();
            }
        } catch (IOException e) {
            // ServerSocket#closeに失敗しました
            log.error("ServerSocket#close error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * スレッドfinallyの実装を行う.
     */
    @java.lang.Override
    protected final void shutdown() {

        threadPoolExecutor = null;
        closeServerSocket();
        serverSocket = null;
        serverSelector = null;
        if (watcherService != null) {
            watcherService.shutdown();
            watcherService = null;
        }
        exceptionHandler = null;
        executes = null;
    }

    /**
     * 現在実行中のスレッドスナップショット.
     * <pre>
     * トランザクションタイムアウト実装用
     * </pre>
     * @return スレッド集合
     */
    public final Set<CommandExecutionable> getExecutesSnapShot() {

        final Set<CommandExecutionable> s = new HashSet<CommandExecutionable>();
        s.addAll(executes);

        return Collections.unmodifiableSet(s);
    }

    /**
     * ソケットサーバの受信待ちを行う.
     */
    @java.lang.Override
    protected final void work() {

        if (log.isTraceEnabled()) {
            log.trace("START");
        }

        try {
            // セレクターの確認
            final int r = serverSelector.select(serverTimeout);
            if (r <= 0) {
                return;
            }
        } catch (IOException e) {
            // selectでIOExceptionが発生しました 処理は続行します
            log.error("Selector.select error", e);
            return;
        } catch (RuntimeException e) {
            // 例外を投げる事で呼び出される
            // ListenerUncaughtExceptionHandlerでサーバを落とす
            // サーバソケットで異常が発生しました.
            log.error("Selector.select error", e);
            throw e;
        }

        // キーリスト取得
        final Set<SelectionKey> selectedKeys = serverSelector.selectedKeys();
        for (final SelectionKey selectionKey : selectedKeys) {
            // キーの削除
            selectedKeys.remove(selectionKey);

            // 無いと思うが判断する
            if (!selectionKey.isAcceptable()) {
                continue;
            }

            // KEYからチャネルを取得する
            final SelectableChannel selectableChannel = selectionKey.channel();
            final ServerSocketChannel serverSocketChannel = ServerSocketChannel.class.cast(selectableChannel);

            SocketChannel socketChannel = null;

            try {
                socketChannel = serverSocketChannel.accept();
            } catch (IOException e) {
                // acceptでIOExceptionが発生しました 処理は続行します
                log.error("SocketChannel.accept error", e);

                return;
            } catch (RuntimeException e) {
                // 例外を投げる事で呼び出される
                // ListenerUncaughtExceptionHandlerでサーバを落とす
                // サーバソケットで異常が発生しました.
                log.error("SocketChannel.accept error", e);
                throw e;
            }

            if (socketChannel == null) {
                continue;
            }

            // 呼び出しているメソッドでの例外は無い ノンブロックの場合はスレッドの中で何とかしてください
            final AbstractListenerCommand command = createListenerCommand(socketChannel.socket());
            command.setExecutes(executes); // 実行中スレッド管理プール

            // RejectedExecutionException はHandlerが在るので発生しない
            // シャットダウン中の接続はRejectedExecutionHandlerでクローズされる
            threadPoolExecutor.execute(command);
        }
    }

    /**
     * サーバソケットの生成を行う.
     * <pre>
     * </pre>
     * @return ソケットサーバ
     */
    private ServerSocket createServerSocket() {

        if (log.isTraceEnabled()) {
            log.trace("START");
        }

        ServerSocket server = null;

        try {
            // バインドアドレスの引数用
            InetAddress inetAddress = null;

            // 存在する場合は生成する
            if (bindAddress != null && bindAddress.length() != 0) {
                inetAddress = InetAddress.getByName(bindAddress);
            }

            // 非同期接続でも対応できるようにしておく
            final SocketAddress socketAddress = new InetSocketAddress(inetAddress, serverPort);
            final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            server = serverSocketChannel.socket();

            // 生成
            server.bind(socketAddress, serverBackLog);

            // タイムアウト設定
            server.setSoTimeout(serverTimeout);

            // ブロックモード設定
            serverSocketChannel.configureBlocking(blocking);

            // セレクターを設定する
            final SelectionKey selectionKey = serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);

            // 受信登録を行う
            selectionKey.interestOps(SelectionKey.OP_ACCEPT);

        } catch (Exception e) {
            // NumberFormatException
            // UnknownHostException
            // SocketException
            // IOException

            // ServerSocketの生成に失敗しました port[{0}] ip[{1}]
            //     time[{2}] back[{3}]
            log.error("ServerSocket open error", e);

            throw new RuntimeException(e);
        }

        if (log.isInfoEnabled()) {
            log.info("ServerSocket open:" + this.toString());
        }

        return server;
    }

// =============================================================================
// MBeanでの監視用委譲メソッド
// =============================================================================

    /**
     * タスクをアクティブに実行中のスレッドの概数を返します。<br />
     * @return スレッド数
     */
    public final int getActiveCount() {
        if (threadPoolExecutor == null) {
            return -1;
        }
        return threadPoolExecutor.getActiveCount();
    }

    /**
     * 実行が完了したタスクのおおよその総数を返します。<br />
     * @return 実行が完了したタスクのおおよその総数
     */
    public final long getCompletedTaskCount() {
        if (threadPoolExecutor == null) {
            return -1;
        }
        return threadPoolExecutor.getCompletedTaskCount();
    }

    /**
     * これまで同時にプールに存在したスレッドの最大数を返します。<br />
     * @return 同時にプールに存在したスレッドの最大数
     */
    public final int getLargestPoolSize() {
        if (threadPoolExecutor == null) {
            return -1;
        }
        return threadPoolExecutor.getLargestPoolSize();
    }

    /**
     * 許可されるスレッドの最大数を返します。<br />
     * @return 許可されるスレッドの最大数を返します
     */
    public final int getMaximumPoolSize() {
        if (threadPoolExecutor == null) {
            return -1;
        }
        return threadPoolExecutor.getMaximumPoolSize();
    }

    /**
     * プール内の現在のスレッド数を返します。<br />
     * @return プール内の現在のスレッド数を返します。
     */
    public final int getPoolSize() {
        if (threadPoolExecutor == null) {
            return -1;
        }
        return threadPoolExecutor.getPoolSize();
    }

    /**
     * 実行がスケジュールされたタスクのおおよその総数を返します。<br />
     * @return 実行がスケジュールされたタスクのおおよその総数を返します。
     */
    public final long getTaskCount() {
        if (threadPoolExecutor == null) {
            return -1;
        }
        return threadPoolExecutor.getTaskCount();
    }

    /**
     * スレッド障害ハンドラ.
     */
    private static class AbortExceptionHandler implements UncaughtExceptionHandler {

        /** 値 */
        private AbstractServerSocketListener listener;

        /**
         * コンストラクタ.
         * @param listener
         */
        public AbortExceptionHandler(final AbstractServerSocketListener listener) {
            this.listener = listener;
        }

        /**
         * 障害ハンドラ.
         * @param t スレッド
         * @param e 例外
         */
        public void uncaughtException(final Thread t, final Throwable e) {

            listener.shutdown();

            return;
        }
    }


// =============================================================================
// 継承先が実装する物
// =============================================================================

    /**
     * 決して例外を起こしてはならない
     */
    protected abstract AbstractListenerCommand createListenerCommand(final Socket socket);

}
