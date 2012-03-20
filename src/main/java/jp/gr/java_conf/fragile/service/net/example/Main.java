/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net.example;

import jp.gr.java_conf.fragile.service.net.AbstractServerSocketListener;
import jp.gr.java_conf.fragile.service.net.pool.ServerSocketExecutorFactory;

/**
 * サンプル実行
 */
public class Main {

    /**
     * <p>メイン</p>
     * @param arg 引数
     */
    public static void main(final String[] arg) {
        try {
            System.out.println("start");
            new Main().execute(arg);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("end");
        }
        return;
    }

    /**
     * <p>インスタンスエントリ</p>
     * @param arg 引数
     * @throws Exception 異常
     */
    public void execute(final String[] arg) throws Exception {
        System.out.println("start");
        try {

            // インスタンスの組み立てはDIコンテナを想定してPOJOです(どんなコンテナでもいけるはず)
            AbstractServerSocketListener serverSocketListener = new SampleServerSocketListener();
            // ComponentFactory.createFactory(SampleServerSocketListener.class).getComponent("ServerSocketListener");

            // DIコンテナ無しでの組み立てサンプル
            ServerSocketExecutorFactory serverSocketExecutorFactory = new ServerSocketExecutorFactory();
            serverSocketExecutorFactory.setExecutorCorePoolSize(10);
            serverSocketExecutorFactory.setExecutorMaximumPoolSize(20);
            serverSocketExecutorFactory.setExecutorKeepAliveTime(10000);
            serverSocketExecutorFactory.setExecutorPoolCapacity(60);

            serverSocketListener = new SampleServerSocketListener();
            serverSocketListener.setBindAddress(null);
            serverSocketListener.setServerPort(8081);
            serverSocketListener.setServerBackLog(60);
            serverSocketListener.setServerTimeout(10000);
            serverSocketListener.setBlocking(false);
            serverSocketListener.setWatcherCommandTimeout(600000);
            serverSocketListener.setWatcherDelay(9223372036854775807L);
            serverSocketListener.setExecutorFactory(serverSocketExecutorFactory);


            serverSocketListener.initialize();

            final Thread thread = new Thread(serverSocketListener, "ServerSocketListener");
            thread.setUncaughtExceptionHandler(serverSocketListener.getExceptionHandler());
            thread.start();

        } catch (final Exception e) {
            throw e;
        } finally {
            System.out.println("end");
        }
    }
}
