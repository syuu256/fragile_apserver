/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net.pool;

import jp.gr.java_conf.fragile.service.net.AbstractListenerCommand;

/**
 * アプリケーションスレッドクラス実装.
 * <pre>
 * 現在実行中のスレッドのコマンドを捕獲する為、本実装を記述する
 * ※ 外部から実行中の処理IPアドレス一覧を取得可能とする
 * ※ このクラスのインスタンスはThreadGroupから取得可能
 * </pre>
 */
public final class ServerSocketPoolThread extends Thread {

    /** rnu時に設定される */
    private AbstractListenerCommand target = null;

    /**
     * コンストラクタ.
     * @param group スレッドグループ
     * @param target Runnable
     */
    public ServerSocketPoolThread(final ThreadGroup group, final Runnable target) {
        super(group, target);
    }

    /**
     * AbstractListenerCommandを取得する.
     * @return runnableMessage を戻します。
     */
    public AbstractListenerCommand getListenerCommand() {
        return target;
    }

    /**
     * コマンドを設定する.
     * @param command コマンド
     */
    public void setListenerCommand(final AbstractListenerCommand command) {
        this.target = command;
    }
}
