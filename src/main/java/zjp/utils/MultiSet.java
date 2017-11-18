package zjp.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/10/16.
 */
public class MultiSet {
    public static <K, V> boolean put(Map<K, Set<V>> map, K key, V value) {
        Set<V> set = map.get(key);
        if (set == null) {
            set = new HashSet<V>();
            map.put(key, set);
        }
        return set.add(value);
    }

    public static <K, V> void putAll(Map<K, Set<V>> map, K key, Iterable<V> values) {
        boolean putSomething = false;
        for (V val : values) {
            put(map, key, val);
            putSomething = true;
        }
        if (!putSomething && !map.containsKey(key)) {
            map.put(key, new HashSet<V>());
        }
    }

    /** Invert the mapping */
    public static <K, V> Map<V, Set<K>> invert(Map<K, Set<V>> map) {
        Map<V, Set<K>> inv = new HashMap<V, Set<K>>();
        for (Map.Entry<K, Set<V>> ent : map.entrySet()) {
            K key = ent.getKey();
            for (V val : ent.getValue()) {
                put(inv, val, key);
            }
        }
        return inv;
    }
}
