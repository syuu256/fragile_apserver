/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net.example;

import java.net.InetAddress;
import java.net.Socket;

import jp.gr.java_conf.fragile.service.net.AbstractListenerCommand;


/**
 * サーバソケットの端末接続単位の処理スレッド.
 */
class SampleListenerCommand extends AbstractListenerCommand {

    /** ソケット */
    private Socket socket = null;

    /**
     * コンストラクタ
     * @param socket
     */
    public SampleListenerCommand(final Socket socket) {

        this.socket = socket;

        return;
    }


    /**
     * 強制停止処理.<br />
     * <pre>
     * HTTPステータスの503などを想定
     * RejectedExecutionHandlerから呼ばれる
     * </pre>
     */
    @Override
    public void abort() {

        try {
            socket.getOutputStream().write("EXIT\r\n".getBytes("Windows-31J"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * アプリ処理.
     * <pre>
     * ノンブロックの場合は別スレッドで監視しているセレクタに登録すればよい？？？
     * </pre>
     */
    @Override
    public void execute() {

        try {
            final byte[] b = new byte[1024];
            final int len = socket.getInputStream().read(b);
            socket.shutdownInput();

            InetAddress inetAddress = socket.getInetAddress();
            socket.getLocalPort();


            String s = new String(b, 0, len, "Windows-31J");

            System.out.println(inetAddress + "/data:" + s);

            socket.getOutputStream().write("OK\r\n".getBytes("Windows-31J"));
            socket.shutdownOutput();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
