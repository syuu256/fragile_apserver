/*
 * Copyright(C) 2009 syuu256\gmail.com. All Rights Reserved.
 */
package jp.gr.java_conf.fragile.commons.collect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 拡張コレクションライブラリ.<br />
 */
public final class Collect {

    /** コンストラクタ. */
    private Collect(){}

    /**
     * HashMapの生成.<br />
     * @param <K> キー
     * @param <V> 値
     * @return 新規インスタンス
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    /**
     * HashMapの生成.<br />
     * @param <K> キー
     * @param <V> 値
     * @param key キー
     * @param value 値
     * @return 新規インスタンス
     */
    public static <K, V> HashMap<K, V> newHashMap(final K key, final V value) {
        final HashMap<K, V> map = newHashMap();
        map.put(key, value);
        return map;
    }

    /**
     * EnumMapの生成.<br />
     * @param <K> キー
     * @param <V> 値
     * @param type 値のClass
     * @return 新規インスタンス
     */
    public static <K extends Enum<K>, V> EnumMap<K, V> newEnumMap(final Class<K> type) {
        return new EnumMap<K, V>(checkNotNull(type));
    }

    /**
     * ArrayListの生成.<br />
     * @param <E> 型
     * @return 新規インスタンス
     */
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }

    /**
     * ArrayListの生成.<br />
     * @param <E> 型
     * @param copyList 初期値
     * @return 新規インスタンス
     */
    public static <E> ArrayList<E> newArrayList(final Collection<? extends E> copyList) {
        return new ArrayList<E>(copyList);
    }

    /**
     * ArrayListの生成.<br />
     * @param <E> 型
     * @param values 初期値
     * @return 新規インスタンス
     */
    public static <E> ArrayList<E> newArrayList(final E ... values) {
        final ArrayList<E> list = newArrayList();
        for (final E value : values)
            list.add(value);
        return list;
    }

    /**
     * HashSetの生成.<br />
     * @param <E> 型
     * @return インスタンス
     */
    public static <E> HashSet<E> newHashSet() {
        return new HashSet<E>();
    }

    /**
     * HashSetの生成.<br />
     * @param <E> 型
     * @param collection コレクション
     * @return インスタンス
     */
    public static <E> HashSet<E> newHashSet(final Collection<? extends E> collection) {
        return new HashSet<E>(collection);
    }

    /**
     * nullチェック.<br />
     * @param <T> 型
     * @param reference 参照
     * @return 引数の値
     */
    public static <T> T checkNotNull(final T reference) {
        if (reference == null)
            throw new NullPointerException();
        return reference;
    }

    /**
     * TreeMapの生成.<br />
     * @param <K> キー
     * @param <V> 値
     * @return 新規インスタンス
     */
    public static <K, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<K, V>();
    }

    /**
     * TreeSetの生成
     * @param <E> 値
     * @return インスタンス
     */
    public static <E> TreeSet<E> newTreeSet() {
        return new TreeSet<E>();
    }

    /**
     * TreeSetの生成
     * @param <E> 値
     * @param collection コレクション
     * @return インスタンス
     */
    public static <E> TreeSet<E> newTreeSet(final Collection<? extends E> collection) {
        return new TreeSet<E>(collection);
    }

    /**
     * TreeSetの生成
     * @param <E> 値
     * @param comparator コンパレータ
     * @return インスタンス
     */
    public static <E> TreeSet<E> newTreeSet(final Comparator<? super E> comparator) {
        return new TreeSet<E>(comparator);
    }

// ======================================================================================

    /**
     * クロージャコールバック.<br />
     * @param <T> 型
     */
    public static interface EachFunction<T> {
        /**
         * コールバック.<br />
         * @param value 値
         * @return 引数値
         */
        T apply(final T value);
    }

    /**
     * クロージャコールバック.<br />
     * @param <F> 型 引数
     * @param <T> 型 戻り値
     */
    public static interface TransFunction<F, T> {
        /**
         * コールバック.<br />
         * @param value 値
         * @return 変換後の値
         */
        T apply(final F value);
    }

    /**
     * クロージャ構文対応
     * @param <X> 型
     * @param collection 入力
     * @param function コールバック
     * @return 第一引数
     */
    public static <X> Collection<? extends X> each(final Collection<? extends X> collection, final EachFunction<? super X> function) {
        for (final X value : collection)
            function.apply(value);
        return collection;
    }

    /**
     * 変換する.<br />
     * @param <F> 入力
     * @param <T> 出力
     * @param formList 入力
     * @param function コールバック
     * @return 出力
     */
    public static <F, T> List<T> transform(final List<F> formList, final TransFunction<? super F, T> function) {
        final List<T> returnList = newArrayList();
        for (final F value : formList)
            returnList.add(function.apply(value));
        return returnList;
    }

// ======================================================================================

    /**
     * ハッシュコードのMapに変換する.
     * @param <V> 値型
     * @param set 変換元値
     * @return SetをMap型にしてkeyはハッシュコード
     */
    public static <V> Map<Integer, V> toMap(final Set<V> set) {

        final Map<Integer, V> map = newHashMap();
        each(set, new EachFunction<V>() {
            @Override
            public V apply(V value) {
                return map.put(Integer.valueOf(value.hashCode()), value);
            }
        });

        return map;
    }

    /**
     * ハッシュコードのMapに変換する.
     * @param list 変換元
     * @param <V> 変換元の型
     * @return ListをMap型にしてkeyはハッシュコード
     */
    public static <V> Map<Integer, V> toMap(final List<V> list) {

        final Map<Integer, V> map = newHashMap();
        each(list, new EachFunction<V>() {
            @Override
            public V apply(V value) {
                return map.put(Integer.valueOf(value.hashCode()), value);
            }
        });

        return map;
    }

    /**
     * MapをSetに変換する.<br />
     * 値の重複は存在しない事。
     * @param <V> 型
     * @param map Map
     * @return Set
     */
    public static <V> Set<V> toSet(final Map<?, V> map) {

        return newHashSet(map.values());
    }
}
