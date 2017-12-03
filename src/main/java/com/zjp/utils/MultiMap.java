package com.zjp.utils;

import java.util.*;

/**
 * Created by Administrator on 2017/10/16.
 */
public class MultiMap {
    public static <K, V> boolean put(Map<K, List<V>> map, K key, V value) {
        List<V> set = map.get(key);
        if (set == null) {
            set = new ArrayList<V>();
            map.put(key, set);
        }
        return set.add(value);
    }

    public static <K, V> void putAll(Map<K, List<V>> map, K key, Iterable<V> values) {
        boolean putSomething = false;
        for (V val : values) {
            put(map, key, val);
            putSomething = true;
        }
        if (!putSomething && !map.containsKey(key)) {
            map.put(key, new ArrayList<V>());
        }
    }

    /** Invert the mapping */
    public static <K, V> Map<V, Set<K>> invert(Map<K, ArrayList<V>> map) {
        Map<V, Set<K>> inv = new HashMap<V, Set<K>>();
        for (Map.Entry<K, ArrayList<V>> ent : map.entrySet()) {
            K key = ent.getKey();
            for (V val : ent.getValue()) {
                MultiSet.put(inv, val, key);
            }
        }
        return inv;
    }
}
