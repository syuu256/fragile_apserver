/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.exceptions;

/**
 * ランタイム例外クラス.<br />
 */
public class FGRuntimeException extends RuntimeException {

    /** シリアルバージョンID. */
    private static final long serialVersionUID = -3254673897872394652L;

    /** ステータスコード. */
    private final String statusCd;

    /** 引数, */
    private final Object[] argument;

    /**
     * コンストラクタ.
     */
    public FGRuntimeException() {
        this(null, "", "", new Object[0]);
    }

    /**
     * コンストラクタ.
     * @param message メッセージ
     */
    public FGRuntimeException(final String message) {
        this(null, message, "", new Object[0]);
    }

    /**
     * コンストラクタ.
     * @param e 例外
     */
    public FGRuntimeException(final Throwable e) {
        this(e, e.toString(), "", new Object[0]);
    }

    /**
     * コンストラクタ.
     * @param message  メッセージ
     * @param e 例外
     */
    public FGRuntimeException(final String message, final Throwable e) {
        this(e, message, "", new Object[0]);
    }

    /**
     * コンストラクタ.
     * @param e 例外
     * @param message  メッセージ
     * @param statusCd ステータスコード
     * @param argument パラメータ
     */
    public FGRuntimeException(final Throwable e, final String message, final String statusCd, final Object... argument) {
        super(message, e);
        this.statusCd = statusCd;
        this.argument = argument;
    }

    /**
     * argumentを取得する.
     * @return argument を戻します。
     */
    public Object[] getArgument() {
        return argument;
    }

    /**
     * statusCdを取得する.
     * @return statusCd を戻します。
     */
    public String getStatusCd() {
        return statusCd;
    }
}
