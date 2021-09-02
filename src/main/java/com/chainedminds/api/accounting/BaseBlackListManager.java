package com.chainedminds.api.accounting;

import com.chainedminds.BaseConfig;
import com.chainedminds.dataClasses.BlockClass;
import com.chainedminds.utilities.TaskManager;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BaseBlackListManager {

    public static final String TYPE_USER_ID = "UserID";
    public static final String TYPE_UUID = "UUID";
    public static final String TYPE_IP_ADDRESS = "IPAddress";
    public static final String TYPE_FIREBASE_ID = "FirebaseID";

    public static final String REASON_WARN = "WARN";
    public static final String REASON_GHOST = "GHOST";
    public static final String REASON_BAN = "BAN";
    public static final String REASON_LEAVE_GAMES = "LeaveGames";

    public static final int STATE_FREE = 0;
    public static final int STATE_WARNED = 1;
    public static final int STATE_GHOST = 2;
    public static final int STATE_BANNED = 3;
    public static final int STATE_DESERTED = 4;

    private static final String TAG = BaseBlackListManager.class.getSimpleName();

    protected static final String FIELD_PROPERTY = "Property";
    protected static final String FIELD_TYPE = "Type";
    protected static final String FIELD_REASON = "Reason";
    protected static final String FIELD_PAYLOAD = "Payload";
    protected static final String FIELD_REPORTER_ID = "ReporterID";
    protected static final String FIELD_BLOCKED_TIMES = "BlockedTimes";
    protected static final String FIELD_LAST_BLOCK_TIME = "LastBlockTime";

    protected static final List<BlockClass> BLOCKS = new ArrayList<>();
    protected static final Set<String> PROPERTY_IDX = new HashSet<>();
    protected static final Map<Object, Integer> REPORTED_PLAYERS = new HashMap<>();

    protected static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public static void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("BlackListManager")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 10, 0)
                .setTimingListener(task -> fetch())
                .startAndSchedule());

        TaskManager.addTask(TaskManager.Task.build()
                .setName("ResetReportedPlayers")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 5, 0)
                .setTimingListener(task -> REPORTED_PLAYERS.clear())
                .startAndSchedule());
    }

    private static void fetch() {

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_BLACK_LIST +
                " ORDER BY " + FIELD_LAST_BLOCK_TIME + " DESC LIMIT 2000";

        BaseDatabaseHelperOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final List<BlockClass> blocks = new ArrayList<>();
            private final Set<String> propertyIdx = new HashSet<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    String property = resultSet.getString(FIELD_PROPERTY);
                    String reason = resultSet.getString(FIELD_REASON);
                    int blockedTimes = resultSet.getInt(FIELD_BLOCKED_TIMES);
                    long lastBlockTime = resultSet.getTimestamp(FIELD_LAST_BLOCK_TIME).getTime();

                    BlockClass block = new BlockClass();

                    block.property = property;
                    block.type = resultSet.getString(FIELD_TYPE);
                    block.reason = reason;
                    block.payload = resultSet.getString(FIELD_PAYLOAD);
                    block.reporterID = resultSet.getInt(FIELD_REPORTER_ID);
                    block.blockedTimes = blockedTimes;
                    block.lastBlockTime = lastBlockTime;

                    int isStillBlocked = isStillBocked(reason, blockedTimes, lastBlockTime);

                    if (isStillBlocked != STATE_FREE) {

                        blocks.add(block);

                        propertyIdx.add(property);
                    }
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        BLOCKS.clear();
                        PROPERTY_IDX.clear();

                        BLOCKS.addAll(blocks);
                        PROPERTY_IDX.addAll(propertyIdx);
                    });
                }
            }
        });
    }

    public static int isBlocked(String property, String type, String reason) {

        AtomicBoolean propertyExists = new AtomicBoolean();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            propertyExists.set(PROPERTY_IDX.contains(property));
        });

        //-------------------------------

        if (propertyExists.get()) {

            BlockClass block = findBlock(property, type, reason);

            if (block == null) {

                return STATE_FREE;
            }

            return isStillBocked(block.reason, block.blockedTimes, block.lastBlockTime);
        }

        return STATE_FREE;
    }

    public static long isMutedTill(int userID, String UUID) {

        BlockClass blockUserID = findBlock(userID + "", TYPE_USER_ID, REASON_GHOST);

        BlockClass blockUUID = findBlock(UUID, TYPE_UUID, REASON_GHOST);

        int blockedTimes = 0;

        long blockedTill = 0;

        if (blockUserID != null) {

            blockedTimes = blockUserID.blockedTimes;

            blockedTill = blockUserID.lastBlockTime;
        }

        if (blockUUID != null) {

            if (blockedTimes < blockUUID.blockedTimes) blockedTimes = blockUUID.blockedTimes;

            if (blockedTill < blockUUID.lastBlockTime) blockedTill = blockUUID.lastBlockTime;
        }

        return blockedTill + (blockedTimes * (BaseConfig.ONE_HOUR * 2));
    }

    private static BlockClass findBlock(String property, String type, String reason) {

        AtomicReference<BlockClass> block = new AtomicReference<>();

        if (property != null){

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                for (BlockClass loopingBlock : BLOCKS) {

                    if (type.equals(loopingBlock.type) &&
                            property.equals(loopingBlock.property) &&
                            (reason.equals("*") || reason.equals(loopingBlock.reason))) {

                        block.set(loopingBlock);

                        break;
                    }
                }
            });
        }

        return block.get();
    }

    public static void addIPAddress(int reporterID, String ipAddress, String reason, String payload) {

        String insertStatement = "INSERT " + BaseConfig.TABLE_BLACK_LIST +
                " (" + FIELD_PROPERTY + ", " + FIELD_TYPE + ", " + FIELD_REASON + ", " +
                FIELD_PAYLOAD + ", " + FIELD_REPORTER_ID + ", " + FIELD_LAST_BLOCK_TIME +
                ") VALUES (?, ?, ?, ?, ?, NOW())  ON DUPLICATE KEY UPDATE " + FIELD_BLOCKED_TIMES +
                " = " + FIELD_BLOCKED_TIMES + " + 1, " + FIELD_LAST_BLOCK_TIME + " = NOW(), " +
                FIELD_REASON + " = VALUES(" + FIELD_REASON + "), " + FIELD_PAYLOAD + " = VALUES(" +
                FIELD_PAYLOAD + "), " + FIELD_REPORTER_ID + " = VALUES (" + FIELD_REPORTER_ID + ")";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, ipAddress);
        parameters.put(2, TYPE_IP_ADDRESS);
        parameters.put(3, reason);
        parameters.put(4, payload);
        parameters.put(5, reporterID);

        BaseDatabaseHelperOld.insert(TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

            BlockClass block = findBlock(ipAddress, TYPE_IP_ADDRESS, reason);

            if (block == null) {

                block = new BlockClass();
            }

            block.property = ipAddress;
            block.type = TYPE_IP_ADDRESS;
            block.reason = reason;
            block.payload = payload;
            block.reporterID = reporterID;
            block.blockedTimes++;
            block.lastBlockTime = System.currentTimeMillis();

            BlockClass finalBlock = block;

            //-------------------------------

            Utilities.lock(TAG, LOCK.writeLock(), () -> {

                BLOCKS.add(finalBlock);

                PROPERTY_IDX.add(ipAddress);
            });
        });
    }

    public static void addUserID(int reporterID, int targetID, String reason, String payload) {

        String insertStatement = "INSERT " + BaseConfig.TABLE_BLACK_LIST + " (" + FIELD_PROPERTY + ", " +
                FIELD_TYPE + ", " + FIELD_REASON + ", " + FIELD_PAYLOAD + ", " + FIELD_REPORTER_ID + ", " +
                FIELD_LAST_BLOCK_TIME + ") VALUES (?, ?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE " +
                FIELD_BLOCKED_TIMES + " = " + FIELD_BLOCKED_TIMES + " + 1, " + FIELD_LAST_BLOCK_TIME +
                " = NOW(), " + FIELD_REASON + " = VALUES(" + FIELD_REASON + "), " + FIELD_PAYLOAD +
                " = VALUES (" + FIELD_PAYLOAD + "), " + FIELD_REPORTER_ID + " = VALUES (" + FIELD_REPORTER_ID + ")";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, targetID);
        parameters.put(2, TYPE_USER_ID);
        parameters.put(3, reason);
        parameters.put(4, payload);
        parameters.put(5, reporterID);

        BaseDatabaseHelperOld.insert(TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

            AtomicReference<BlockClass> block = new AtomicReference<>();

            BlockClass lastBlock = findBlock(targetID + "", TYPE_USER_ID, reason);

            if (lastBlock == null) {

                lastBlock = new BlockClass();
            }

            lastBlock.property = targetID + "";
            lastBlock.type = TYPE_USER_ID;
            lastBlock.reason = reason;
            lastBlock.payload = payload;
            lastBlock.reporterID = reporterID;
            lastBlock.blockedTimes++;
            lastBlock.lastBlockTime = System.currentTimeMillis();

            block.set(lastBlock);

            //-------------------------------

            Utilities.lock(TAG, LOCK.writeLock(), () -> {

                BLOCKS.add(block.get());

                PROPERTY_IDX.add(targetID + "");
            });
        });
    }

    protected boolean onVelocityCheck(Object property, String type, String reason) {

        return false;
    }

    protected void onVelocityAlert(Object property, String type, String reason, String payload) {

    }

    public void addBlockRecord(int reporterID, Object property, String type, String reason, String payload) {

        String insertStatement = "INSERT " + BaseConfig.TABLE_BLACK_LIST +
                " (" + FIELD_PROPERTY + ", " + FIELD_TYPE + ", " + FIELD_REASON + ", " +
                FIELD_PAYLOAD + ", " + FIELD_REPORTER_ID + ", " + FIELD_LAST_BLOCK_TIME +
                ") VALUES (?, ?, ?, ?, ?, NOW())  ON DUPLICATE KEY UPDATE " + FIELD_BLOCKED_TIMES +
                " = " + FIELD_BLOCKED_TIMES + " + 1, " + FIELD_LAST_BLOCK_TIME + " = NOW(), " +
                FIELD_REASON + " = VALUES(" + FIELD_REASON + "), " + FIELD_PAYLOAD + " = VALUES(" +
                FIELD_PAYLOAD + "), " + FIELD_REPORTER_ID + " = VALUES (" + FIELD_REPORTER_ID + ")";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, property);
        parameters.put(2, type);
        parameters.put(3, reason);
        parameters.put(4, payload);
        parameters.put(5, reporterID);

        BaseDatabaseHelperOld.insert(TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

            BlockClass block = findBlock(property + "", type, reason);

            if (block == null) {

                block = new BlockClass();
            }

            block.property = property + "";
            block.type = type;
            block.reason = reason;
            block.payload = payload;
            block.reporterID = reporterID;
            block.blockedTimes++;
            block.lastBlockTime = System.currentTimeMillis();

            BlockClass finalBlock = block;

            //-------------------------------

            Utilities.lock(TAG, LOCK.writeLock(), () -> {

                BLOCKS.add(finalBlock);

                PROPERTY_IDX.add(property + "");
            });

            //-------------------------------

            if (onVelocityCheck(property, type, reason)) {

                onVelocityAlert(property, type, reason, payload);
            }
        });
    }

    private static int isStillBocked(String reason, int blockedTimes, long lastBlockTime) {

        if (REASON_LEAVE_GAMES.equals(reason)) {

            //FIXME : HAS LOGICAL PROBLEMS

            long blockDuration = blockedTimes * (5 * 60 * 1000);

            if (System.currentTimeMillis() - blockDuration < lastBlockTime) return STATE_DESERTED;
        }

        if (REASON_WARN.equals(reason)) {

            long blockDuration = blockedTimes * BaseConfig.FIVE_MINUTES;

            if (System.currentTimeMillis() - blockDuration < lastBlockTime) return STATE_WARNED;
        }

        if (REASON_GHOST.equals(reason)) {

            long blockDuration = blockedTimes * BaseConfig.ONE_HOUR * 2;

            if (System.currentTimeMillis() - blockDuration < lastBlockTime) return STATE_GHOST;
        }

        if (REASON_BAN.equals(reason)) {

            if (System.currentTimeMillis() - BaseConfig.ONE_DAY * 7 < lastBlockTime) return STATE_BANNED;
        }

        return STATE_FREE;
    }

    public static void cleanupTable() {

        // TODO : REDUCE THE SANCTIONS AND BLOCK LEVEL FOR USERS WHEN TIME PASTS THE BLOCK TIME ONE BY ONE

        /*long timePast = System.currentTimeMillis() - lastBlockTime;

        if (REASON_LEAVE_GAMES.equals(reason)) {

            if (blockedTimes >= 10 && timePast == TWO_DAYS) {

                blockedTimes = 8;

            } else if (blockedTimes >= 8 && timePast == X_HOURS) {

                blockedTimes = 4;

            } else if (timePast == X_HOURS) {

                blockedTimes = 0;
            }
        }*/
    }

    public static void removeFromCache(String property) {

        PROPERTY_IDX.remove(property);
    }
}