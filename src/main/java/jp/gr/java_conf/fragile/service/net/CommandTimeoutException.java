/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.service.net;

/**
 * コマンドがタイムアウトした場合に投げる.
 */
public class CommandTimeoutException extends RuntimeException {

    /** serialVersionUID */
    private static final long serialVersionUID = -5077805148039261615L;

    /**
     * コンストラクタ.
     * @param message
     */
    public CommandTimeoutException(final String message) {
        super(message);
    }

    /**
     * コンストラクタ.
     * @param message
     * @param cause
     */
    public CommandTimeoutException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * コンストラクタ.
     * @param cause
     */
    public CommandTimeoutException(final Throwable cause) {
        super(cause);
    }

}
