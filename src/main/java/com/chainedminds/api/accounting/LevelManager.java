package com.chainedminds.api.accounting;

import com.chainedminds.BaseConfig;
import com.chainedminds.utilities.TaskManager;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database.DatabaseHelper;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LevelManager {

    private static final String TAG = LevelManager.class.getSimpleName();

    private final static String FIELD_APP_NAME = "AppName";
    private final static String FIELD_LEVEL = "Level";
    private final static String FIELD_SCORE = "Score";

    private static final Map<String, List<Integer>> LEVEL_SCORES = new HashMap<>();

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public static void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("LevelManager")
                .setTime(0, 0, 0)
                .setInterval(0, 1, 0, 0)
                .setTimingListener(task -> fetch())
                .startAndSchedule());
    }

    private static void fetch() {

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_LEVELS + " ORDER BY " + FIELD_LEVEL;

        DatabaseHelper.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final Map<String, List<Integer>> levelScores = new HashMap<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    String appName = resultSet.getString(FIELD_APP_NAME);
                    int score = resultSet.getInt(FIELD_SCORE);

                    levelScores.putIfAbsent(appName, new ArrayList<>());

                    levelScores.get(appName).add(score);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        LEVEL_SCORES.clear();

                        for (String appName : levelScores.keySet()) {

                            LEVEL_SCORES.put(appName, levelScores.get(appName));
                        }
                    });
                }
            }
        });
    }

    public static int getLevel(String appName, int score) {

        AtomicInteger level = new AtomicInteger();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (int levelScore : LEVEL_SCORES.getOrDefault(appName, new ArrayList<>())) {

                if (score < levelScore) {

                    break;
                }

                level.getAndIncrement();
            }
        });

        return level.get();
    }

    public static int getNextLevelScore(String appName, int score) {

        AtomicInteger nextLevelScore = new AtomicInteger();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (int levelScore : LEVEL_SCORES.getOrDefault(appName, new ArrayList<>())) {

                if (levelScore > score) {

                    nextLevelScore.set(levelScore);

                    break;
                }
            }
        });

        return nextLevelScore.get();
    }

    public static int getCurrentLevelScore(String appName, int score) {

        AtomicInteger lastScore = new AtomicInteger();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            int loopingScore = 0;

            for (int levelScore : LEVEL_SCORES.getOrDefault(appName, new ArrayList<>())) {

                if (levelScore > score) {

                    break;
                }

                loopingScore = levelScore;
            }

            lastScore.set(loopingScore);
        });

        return lastScore.get();
    }
}