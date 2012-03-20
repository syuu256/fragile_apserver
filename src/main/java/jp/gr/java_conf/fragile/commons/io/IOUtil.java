/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * io関連のユーティリティー
 */
public class IOUtil {

    /** バッファサイズ */
    private static final int BUFFER_SIZE = 1024;

    /**
     * InputStreamをbyte[]に変換する.
     * @param inputStream 入力
     * @return バイト配列
     * @throws IOException　例外
     */
    public byte[] toByteArray(final InputStream inputStream) throws IOException {

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] returnValue = null;
        try {
            final byte[] buffer = new byte[BUFFER_SIZE];
            int length = 0;
            while ((length = inputStream.read(buffer, 0, buffer.length)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            byteArrayOutputStream.flush();
            returnValue = byteArrayOutputStream.toByteArray();
        } finally {
            byteArrayOutputStream.close();
        }
        return returnValue;
    }
}
