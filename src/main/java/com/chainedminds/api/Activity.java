package com.chainedminds.api;

import com.chainedminds._Config;
import com.chainedminds.api.accounting._AccountSession;
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

            case PLAYING_DB:

                return Messages.get(Messages.ACTIVITIES, Messages.Activities.PLAYING_DB, language);

            case PLAYING_C4:

                return Messages.get(Messages.ACTIVITIES, Messages.Activities.PLAYING_C4, language);

            case PLAYING_TF:

                return Messages.get(Messages.ACTIVITIES, Messages.Activities.PLAYING_TF, language);

            case PLAYING_BB:

                return Messages.get(Messages.ACTIVITIES, Messages.Activities.PLAYING_BB, language);

            case PLAYING_MCH:

                return Messages.get(Messages.ACTIVITIES, Messages.Activities.PLAYING_MCH, language);

            case PLAYING_QW:

                return Messages.get(Messages.ACTIVITIES, Messages.Activities.PLAYING_QW, language);

            case PLAYING_FF:

                return Messages.get(Messages.ACTIVITIES, Messages.Activities.PLAYING_FF, language);

            case PLAYING_WE:

                return Messages.get(Messages.ACTIVITIES, Messages.Activities.PLAYING_WE, language);

            case PLAYING_CR:

                return Messages.get(Messages.ACTIVITIES, Messages.Activities.ONLINE, language);

            default:

                long lastActivity = _AccountSession.USER_ACTIVITY
                        .getOrDefault(appName, new HashMap<>())
                        .getOrDefault(userID, 0L);

                long diff = System.currentTimeMillis() - lastActivity;

                if (diff > _Config.ONE_YEAR) {

                    return Messages.get(Messages.ACTIVITIES, Messages.Activities.WAS_ONLINE_YEARS_AGO,
                            language, "" + Math.round(diff / _Config.ONE_YEAR));
                }
                if (diff > _Config.ONE_MONTH) {

                    return Messages.get(Messages.ACTIVITIES, Messages.Activities.WAS_ONLINE_MONTHS_AGO,
                            language, "" + Math.round(diff / _Config.ONE_MONTH));
                }
                if (diff > _Config.ONE_DAY) {

                    return Messages.get(Messages.ACTIVITIES, Messages.Activities.WAS_ONLINE_DAYS_AGO,
                            language, "" + Math.round(diff / _Config.ONE_DAY));
                }
                if (diff > _Config.ONE_HOUR) {

                    return Messages.get(Messages.ACTIVITIES, Messages.Activities.WAS_ONLINE_HOURS_AGO,
                            language, "" + Math.round(diff / _Config.ONE_HOUR));
                }
                if (diff > _Config.ONE_MINUTE) {

                    return Messages.get(Messages.ACTIVITIES, Messages.Activities.WAS_ONLINE_MINUTES_AGO,
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
        PLAYING_DB,
        PLAYING_C4,
        PLAYING_TF,
        PLAYING_BB,
        PLAYING_MCH,
        PLAYING_QW,
        PLAYING_FF,
        PLAYING_WE,
        PLAYING_CR
    }
}