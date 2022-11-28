package com.chainedminds.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheManager<Key, Value> {

    private static final String TAG = CacheManager.class.getSimpleName();

    private static final List<CacheManager> cacheManagers = new ArrayList<>();

    private final Map<Key, Data> cache = new HashMap<>();

    private long RECORD_LIFETIME = 60 * 60 * 1000;

    public CacheManager(long recordLifetime) {

        this.RECORD_LIFETIME = recordLifetime;

        cacheManagers.add(this);
    }

    public CacheManager() {

        cacheManagers.add(this);
    }

    public static void startCacheCleaner() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("CacheManager")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 1, 0)
                .setTimingListener(task -> cacheManagers.forEach(CacheManager::cleanCache)));
    }

    /*public V get(K key, NotExists<V> notExists) {

        if (cache.containsKey(key)) {

            return cache.get(key);
        }

        V value = notExists.add();

        put(key, value);

        return value;
    }*/

    /*public Value get(Key key, Value defaultValue) {

        if (cache.containsKey(key)) {

            return extractValue(cache.get(key));

        } else {

            put(key, defaultValue);
        }

        return defaultValue;
    }*/

    //ONLY USE WHEN YOU ARE SURE THERE IS DATA IN CACHE OR YOU NEED A NULL CHECK!!!
    public Value get(Key key) {

        return get(key, null);
    }

    public Value get(Key key, OnNoDataAvailableListener<Value> dataFetcher) {

        Data storedData = cache.get(key);

        if (storedData != null && storedData.value != null) {

            storedData.lastAccessTime = System.currentTimeMillis();

            return storedData.value;

        } else if (dataFetcher != null) {

            Value value = dataFetcher.retrieve();

            put(key, value);

            return value;

        } else return null;
    }

    public void put(Key key, Value value) {

        Data data = cache.getOrDefault(key, new Data());

        data.value = value;
        data.lastAccessTime = System.currentTimeMillis();

        cache.put(key, data);
    }

    private void cleanCache() {

        long currentTime = System.currentTimeMillis();

        List<Key> keysToBeRemoved = new ArrayList<>();

        cache.forEach((key, data) -> {

            if (currentTime - getLastAccessTime(data) > RECORD_LIFETIME) {

                keysToBeRemoved.add(key);
            }
        });

        keysToBeRemoved.forEach(cache::remove);
    }

    public void invalidateCache() {

        cache.clear();
    }

    private Value extractValue(Data data) {

        return data != null ? data.value : null;
    }

    private long getLastAccessTime(Data data) {

        return data != null ? data.lastAccessTime : 0;
    }

    public interface OnNoDataAvailableListener<V> {

        V retrieve();
    }

    class Data {

        Value value;
        long lastAccessTime;
    }
}
