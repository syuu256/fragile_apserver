/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net.example;

import java.net.Socket;

import jp.gr.java_conf.fragile.service.net.AbstractListenerCommand;
import jp.gr.java_conf.fragile.service.net.AbstractServerSocketListener;


/**
 * ソケットサーバ実装サンプル
 */
public class SampleServerSocketListener extends AbstractServerSocketListener {

    /**
     * 受信単位のコマンドのインスタンスを生成する
     * @param socket
     * @return コマンド
     */
    @Override
    protected AbstractListenerCommand createListenerCommand(final Socket socket) {

        return new SampleListenerCommand(socket);
    }

}
