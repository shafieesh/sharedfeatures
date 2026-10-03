package com.chainedminds.api;

import com.chainedminds._Config;
import com.chainedminds.utilities.Messages;
import com.chainedminds.utilities.Utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class _AccountActivity {

    private static final String TAG = _AccountActivity.class.getSimpleName();

    public static final Map<Integer, Long> LAST_ACCESS = new HashMap<>();
    public static final Map<Integer, String> LAST_ACTIVITY = new HashMap<>();

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public String getLastActivity(int userID, String language) {

        String lastActivity = getLastActivity(userID);

        if (lastActivity != null) {

            return lastActivity;

        } else {

            long lastAccess = getLastAccess(userID);

            long diff = System.currentTimeMillis() - lastAccess;

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

            return Messages.get("ONLINE", language);
        }
    }

    public String getLastActivity(int userID) {

        AtomicReference<String> activity = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.writeLock(), () -> {

            long lastAccess = LAST_ACCESS.getOrDefault(userID, 0L);

            if (System.currentTimeMillis() - lastAccess > _Config.TEN_MINUTES) {

                LAST_ACTIVITY.remove(userID);
            }

            activity.set(LAST_ACTIVITY.get(userID));
        });

        return activity.get();
    }

    public void setLastActivity(int userID, String activity) {

        long currentTime = System.currentTimeMillis();

        Utilities.lock(TAG, LOCK.writeLock(), () -> {

            LAST_ACTIVITY.put(userID, activity);
            LAST_ACCESS.put(userID, currentTime);
        });

        onActivityChanged(userID, activity, currentTime);
    }

    public void onActivityChanged(int userID, String activity, long timestamp) {


    }

    //------------------------------------------------------------------------------------

    public long getLastAccess(int userID) {

        return LAST_ACCESS.getOrDefault(userID, 0L);
    }

    public void setLastAccess(int userID) {

        setLastAccess(userID, System.currentTimeMillis());
    }

    public void setLastAccess(int userID, long lastUpdate) {

        long oldLastUpdate = getLastAccess(userID);

        LAST_ACCESS.put(userID, Math.max(oldLastUpdate, lastUpdate));
    }
}