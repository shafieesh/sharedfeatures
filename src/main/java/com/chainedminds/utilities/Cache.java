package com.chainedminds.utilities;

import java.util.*;

@SuppressWarnings("unused")
public class Cache<Key, Value> {

    private static final List<Cache<?, ?>> CACHE_MANAGERS = new ArrayList<>();

    private final Map<Key, Record<Value>> cache = new HashMap<>();

    private long LIFETIME = 60 * 60 * 1000;

    public Cache(long recordLifetime) {

        this.LIFETIME = recordLifetime;

        CACHE_MANAGERS.add(this);
    }

    public Cache() {

        CACHE_MANAGERS.add(this);
    }

    public static void start() {

        Task.add(Task.Data.build()
                .setName("CacheManager")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 0, 1)
                .setTimingListener(task -> new ArrayList<>(CACHE_MANAGERS).forEach(Cache::cleanStale))
                .schedule());
    }

    public Value get(Key key) {

        return getOrPut(key, null);
    }

    public Value getOrDefault(Key key, Value defaultValue) {

        Value value = getOrPut(key, null);

        return value != null ? value : defaultValue;
    }

    public Value getOrPut(Key key, OnNoDataAvailableListener<Value> dataFetcher) {

        Record<Value> storedRecord = cache.get(key);

        if (storedRecord != null && storedRecord.value != null) {

            storedRecord.lastAccessTime = System.currentTimeMillis();

            return storedRecord.value;

        } else if (dataFetcher != null) {

            Value value = dataFetcher.retrieve();

            put(key, value);

            return value;

        } else return null;
    }

    public void put(Key key, Value value) {

        Record<Value> record = new Record<>(value);

        put(key, record);
    }

    public void put(Key key, Value value, long accessLifetime) {

        Record<Value> record = new Record<>(value).accessLifetime(accessLifetime);

        put(key, record);
    }

    public void put(Key key, Record<Value> record) {

        if (record.accessLifetime == -1) {

            record.accessLifetime = LIFETIME;
        }

        cache.put(key, record);
    }

    public void putIfAbsent(Key key, Value value) {

        Record<Value> record = new Record<>(value);

        putIfAbsent(key, record);
    }

    public void putIfAbsent(Key key, Value value, long accessLifetime) {

        Record<Value> record = new Record<>(value).accessLifetime(accessLifetime);

        putIfAbsent(key, record);
    }

    public void putIfAbsent(Key key, Record<Value> record) {

        if (record.accessLifetime == -1) {

            record.accessLifetime = LIFETIME;
        }

        cache.putIfAbsent(key, record);
    }

    public void setValue(Key key, Value value) {

        Record<Value> storedRecord = cache.get(key);

        if (storedRecord != null) {

            storedRecord.value(value);
        }
    }

    public void remove(Key key) {

        cache.remove(key);
    }

    public void remove(Collection<Key> keys) {

        keys.forEach(cache::remove);
    }

    public void invalidate() {

        cache.clear();
    }

    private void cleanStale() {

        long currentTime = System.currentTimeMillis();

        new HashMap<>(cache).forEach((key, record) -> {

            if (currentTime > record.expirationTime) {

                cache.remove(key);
            }

            if (currentTime - record.lastAccessTime > record.accessLifetime) {

                cache.remove(key);
            }
        });
    }

    public interface OnNoDataAvailableListener<V> {

        V retrieve();
    }

    public static class Record<Value> {

        private Value value;
        private long accessLifetime = -1;
        private long expirationTime = Long.MAX_VALUE;
        private long lastAccessTime = System.currentTimeMillis();

        public Record(Value value) {

            this.value = value;
        }

        public Record<Value> value(Value value) {

            this.value = value;

            return this;
        }

        public Record<Value> accessLifetime(long accessLifetime) {

            if (accessLifetime <= 0) {

                throw new RuntimeException("Access lifetime should be a positive none zero value.");
            }

            this.accessLifetime = accessLifetime;

            return this;
        }

        public Record<Value> expirationTime(long addedTime) {

            long expirationTime = System.currentTimeMillis() + addedTime;

            if (expirationTime <= System.currentTimeMillis()) {

                throw new RuntimeException("Expiration time is passed");
            }

            this.expirationTime = expirationTime;

            return this;
        }
    }
}
