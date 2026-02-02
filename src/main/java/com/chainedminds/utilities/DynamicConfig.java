package com.chainedminds.utilities;

import com.chainedminds._Config;
import com.chainedminds.utilities.database._DatabaseOld;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DynamicConfig {

    private static final String TAG = DynamicConfig.class.getSimpleName();

    private static final String FIELD_KEY = "Key";
    private static final String FIELD_TYPE = "Type";
    private static final String FIELD_VALUE = "Value";

    public static final Map<String, String> MAPS = new HashMap<>();
    public static final Map<String, ArrayList<String>> LISTS = new HashMap<>();

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public static void start() {

        Task.add(Task.Data.build()
                .setName("Dynamic Config")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 5, 0)
                .onEachCycle(DynamicConfig::fetchRecords)
                .runNow()
                .schedule());
    }

    private static void fetchRecords() {

        String selectStatement = "SELECT * FROM " + _Config.TABLE_DYNAMIC_CONFIGS;

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final Map<String, String> maps = new HashMap<>();
            private final Map<String, ArrayList<String>> lists = new HashMap<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    String key = resultSet.getString(FIELD_KEY);
                    String type = resultSet.getString(FIELD_TYPE);
                    String value = resultSet.getString(FIELD_VALUE);

                    if ("list".equals(type)) {

                        lists.putIfAbsent(key, new ArrayList<>());
                        lists.get(key).add(value);
                    }

                    if ("map".equals(type)) {

                        maps.put(key, value);
                    }
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        MAPS.clear();
                        LISTS.clear();

                        MAPS.putAll(maps);
                        LISTS.putAll(lists);
                    });
                }
            }
        });
    }

    public static String getMap(String section, String field, Object defaultValue) {

        String value = getMap(section + "-" + field);

        if (value == null) {

            value = "" + defaultValue;
        }

        return value;
    }

    public static String getMap(String section, String field) {

        return getMap(section + "-" + field);
    }

    public static String getMap(String key) {

        AtomicReference<String> value = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            value.set(MAPS.get(key));
        });

        return value.get();
    }

    public static List<String> getList(String section, String field) {

        return getList(section + "-" + field);
    }

    public static List<String> getList(String key) {

        List<String> values = new ArrayList<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            values.addAll(LISTS.getOrDefault(key, new ArrayList<>()));
        });

        return values;
    }

    public static boolean setMap(String section, String field, String value) {

        String key = section + "-" + field;

        String statement = "REPLACE INTO " + _Config.TABLE_DYNAMIC_CONFIGS + " (`" +
                FIELD_KEY + "`, " + FIELD_TYPE + ", " + FIELD_VALUE + ") VALUES (?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, key);
        parameters.put(2, "map");
        parameters.put(3, value);

        boolean wasSuccessful = _DatabaseOld.insert(TAG, statement, parameters);

        if (wasSuccessful) {

            fetchRecords();
        }

        return wasSuccessful;
    }

    public static void updateRecords() {

        fetchRecords();
    }
}