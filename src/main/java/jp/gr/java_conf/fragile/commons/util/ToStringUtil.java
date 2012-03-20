/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.util;

import java.util.Collection;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * toString文字列を生成する.<br />
 */
public final class ToStringUtil {

    /**
     * コンストラクタ.
     */
    private ToStringUtil() {
    }

    /**
     * ToStringBuilder.reflectionToStringラッパー.<br />
     * @param object 対象
     * @return ダンプ
     */
    public static String buildToString(final Object object) {
        //MULTI_LINE_STYLE
        return ToStringBuilder.reflectionToString(object, NestedMultiLineStyle.INSTANCE);
    }
}

/**
 * ネストインデント付スタイル.
 */
class NestedMultiLineStyle extends ToStringStyle {

    /** シリアルバージョンUID */
    private static final long serialVersionUID = 4209774230409183569L;

    /** 自インスタンス */
    public static final NestedMultiLineStyle INSTANCE = new NestedMultiLineStyle();

    /** インデントスタイル */
    private static final String INDENT_SUFFIX = "  ";

    /** レベル保持 */
    private static final ThreadLocal<Integer> level = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return Integer.valueOf(0);
        }
    };

    /**
     * コンストラクタ
     */
    private NestedMultiLineStyle() {
        super();
        this.setContentStart("{");
        this.setFieldSeparator(SystemUtils.LINE_SEPARATOR + INDENT_SUFFIX);
        this.setFieldSeparatorAtStart(true);
        this.setContentEnd(SystemUtils.LINE_SEPARATOR + "}");
    }

    @Override
    protected void appendFieldStart(StringBuffer buffer, String fieldName) {
        Integer lv = level.get();
        appendIndent(buffer, lv.intValue());
        super.appendFieldStart(buffer, fieldName);
        level.set(Integer.valueOf(lv.intValue() + 1));
    }

    @Override
    protected void appendFieldEnd(StringBuffer buffer, String fieldName) {
        super.appendFieldEnd(buffer, fieldName);
        level.set(Integer.valueOf(level.get().intValue() - 1));
    }

    @Override
    protected void appendContentEnd(StringBuffer buffer) {
        buffer.append(SystemUtils.LINE_SEPARATOR);
        Integer lv = level.get();
        appendIndent(buffer, lv.intValue());
        buffer.append("}");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Collection coll) {
        Integer lv = level.get();
        level.set(Integer.valueOf(lv.intValue() + 1));
        buffer.append("(size:" + coll.size() + ")\n");
        appendIndent(buffer, lv.intValue());
        super.appendDetail(buffer, fieldName, coll);
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append("\n");
        level.set(lv);
        appendIndent(buffer, lv.intValue());
        buffer.append("]");
    }

    private Object readResolve() {
        return INSTANCE;
    }

    private void appendIndent(StringBuffer buffer, int lv) {
        for (int i = 0; i < lv; i++) {
            buffer.append(INDENT_SUFFIX);
        }
    }
}
