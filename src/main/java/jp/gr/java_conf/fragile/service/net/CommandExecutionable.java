/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net;

import java.net.Socket;

/**
 * スレッド強制停止用のインターフェース.
 */
public interface CommandExecutionable {

    /**
     * ソケットを戻す.
     * <pre>
     * 戻り電文を戻す必要があるので取得できるようにする
     * </pre>
     * @return ソケット
     */
    Socket getSocket();

    /**
     * スレッドを開始した時間を戻す.
     * <pre>
     * スレッドの処理時間を計るために取得できるようにする
     * 処理タイムアウト制御に使用
     * </pre>
     * @return 開始時間
     */
    long getStartTime();

    /**
     * 強制終了する場合に割り込みを上げる.
     */
    void interrupt();
}
