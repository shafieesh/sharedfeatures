package com.chainedminds.api;

import com.chainedminds._Config;
import com.chainedminds.api.account._AccountSession;
import com.chainedminds.utilities.Messages;
import com.chainedminds.utilities.Utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Activity {

    private static final String TAG = Activity.class.getSimpleName();

    private static final Map<Integer, Type> LAST_ACTIVITY_CACHE = new HashMap<>();
    private static final Map<Integer, Long> LAST_MODIFICATION = new HashMap<>();

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public static String getLastActivity(int userID, String appName, String language) {

        cleanUpLastAccessTimes();

        AtomicReference<Type> activity = new AtomicReference<>(Type.NOTHING);

        Utilities.lock(TAG, LOCK.writeLock(), () -> {

            activity.set(LAST_ACTIVITY_CACHE.getOrDefault(userID, Type.NOTHING));
        });

        switch (activity.get()) {

            case PLAYING_X:

                return Messages.get("PLAYING_X", language);

            default:

                long lastActivity = _AccountSession.USER_ACTIVITY
                        .getOrDefault(appName, new HashMap<>())
                        .getOrDefault(userID, 0L);

                long diff = System.currentTimeMillis() - lastActivity;

                if (diff > _Config.ONE_YEAR) {

                    return Messages.get("WAS_ONLINE_YEARS_AGO",
                            language, "" + Math.round(diff / _Config.ONE_YEAR));
                }
                if (diff > _Config.ONE_MONTH) {

                    return Messages.get("WAS_ONLINE_MONTHS_AGO",
                            language, "" + Math.round(diff / _Config.ONE_MONTH));
                }
                if (diff > _Config.ONE_DAY) {

                    return Messages.get("WAS_ONLINE_DAYS_AGO",
                            language, "" + Math.round(diff / _Config.ONE_DAY));
                }
                if (diff > _Config.ONE_HOUR) {

                    return Messages.get("WAS_ONLINE_HOURS_AGO",
                            language, "" + Math.round(diff / _Config.ONE_HOUR));
                }
                if (diff > _Config.ONE_MINUTE) {

                    return Messages.get("WAS_ONLINE_MINUTES_AGO",
                            language, "" + Math.round(diff / _Config.ONE_MINUTE));
                }

                return "";
        }
    }

    public static void setLastActivity(int userID, Type type) {

        Utilities.lock(TAG, LOCK.writeLock(), () -> {

            LAST_ACTIVITY_CACHE.put(userID, type);
            LAST_MODIFICATION.put(userID, System.currentTimeMillis());
        });
    }

    private static void cleanUpLastAccessTimes() {

        Utilities.lock(TAG, LOCK.writeLock(), () -> {

            LAST_MODIFICATION.keySet().removeIf(key -> {

                if (System.currentTimeMillis() - LAST_MODIFICATION.getOrDefault(key, 0L) > _Config.TEN_MINUTES) {

                    LAST_ACTIVITY_CACHE.remove(key);

                    return true;
                }

                return false;
            });
        });
    }

    public enum Type {

        NOTHING,
        PLAYING_X,
    }
}