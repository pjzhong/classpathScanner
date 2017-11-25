package com.zjp.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 11/5/2017.
 */
public abstract class SingleTonMap<K, V> {
    private final ConcurrentMap<K, SingletonHolder<V>> map = new ConcurrentHashMap<>();

    public boolean createSingleton(final K key) throws Exception {
        SingletonHolder<V> holder = new SingletonHolder<>();
        final SingletonHolder<V> oldHolder = map.putIfAbsent(key, holder);
        if(oldHolder == null) {
            V newObject = null;
            try {
                newObject = newInstance(key);
                if(newObject == null) {
                    throw new IllegalArgumentException("newInstance(" + key +") return null");
                }
            } finally {
                holder.set(newObject);
            }
            return true;
        } else {
            return false;
        }
    }

    public V get(final K key) throws InterruptedException {
        final SingletonHolder<V> singletonHolder = map.get(key);
        return singletonHolder == null ? null : singletonHolder.get();
    }

    /** Get all singletons in the map. */
    public List<V> values() throws InterruptedException {
        final List<V> entries = new ArrayList<>(map.size());
        for (final Map.Entry<K, SingletonHolder<V>> ent : map.entrySet()) {
            entries.add(ent.getValue().get());
        }
        return entries;
    }

    protected abstract V newInstance(K key);

    /**
     * dig into how ConcurrentHashMap.putIfAbsent() work , to figure out
     * why author need create this SignletoHolder
     * */
    private static class SingletonHolder<V> {
        private V singleton;
        private final CountDownLatch initialized = new CountDownLatch(1);

        public void set(final V singleton) {
            this.singleton = singleton;
            initialized.countDown();
        }

        public V get() throws InterruptedException {
            initialized.await();
            return singleton;
        }
    }
}
