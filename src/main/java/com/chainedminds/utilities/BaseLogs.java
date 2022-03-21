package com.chainedminds.utilities;

import com.chainedminds.BaseConfig;
import com.chainedminds.BaseResources;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseLogs {

    private static final String TAG = BaseLogs.class.getSimpleName();

    protected static final String FIELD_USER_ID = "UserID";
    protected static final String FIELD_SECTION = "Section";
    protected static final String FIELD_ACTION = "Action";
    protected static final String FIELD_CRASH_LOG = "CrashLog";
    protected static final String FIELD_PAYLOAD = "Payload";

    public static void error(String tag, String cause) {

        report(tag, cause, null);
    }

    public static void error(String tag, Throwable throwable) {

        String crashLog = Utilities.Error.toString(throwable);

        report(tag, crashLog, null);
    }

    public static void error(String tag, Throwable throwable, String payload) {

        String crashLog = Utilities.Error.toString(throwable);

        report(tag, crashLog, payload);
    }

    public static void severe(String tag, Throwable throwable) {

        String crashLog = Utilities.Error.toString(throwable);

        report(tag, crashLog, null);

        BaseNotificationManager.reportError(tag, throwable.getMessage());
    }

    public static void severe(String tag, Throwable throwable, String payload) {

        String crashLog = Utilities.Error.toString(throwable);

        report(tag, crashLog, payload);

        BaseNotificationManager.reportError(tag, throwable.getMessage());
    }

    public static void logInfo(String fileName, String content) {

        String fileNameAndExtension = fileName + "-" + System.currentTimeMillis() + ".log";

        BaseResources.getInstance().fileManager.saveFile(BaseFileManager.SECTION_LOG_INFO, fileNameAndExtension, content.getBytes());
    }

    public static boolean log(Connection connection, int userID, String section, String action) {

        String statement = "INSERT " + BaseConfig.TABLE_LOGS +
                " (" + FIELD_USER_ID + ", " + FIELD_SECTION + ", " + FIELD_ACTION + ") VALUES (?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, section);
        parameters.put(3, action);

        return BaseDatabaseHelperOld.insert(connection, TAG, statement, parameters);
    }

    public static void manage(String tag, String payload) {

        report(tag, null, payload);
    }

    protected static void report(String section, String log, String payload) {

        System.out.println(log + "\n\nPAYLOAD :\n" + payload + "\n");

        /*LogData logData = new LogData();
        logData.section = section;
        logData.log = log;
        logData.payload = payload;

        PENDING_LOGS.add(logData);

        if (BaseMonitor.getCpuUsage() > 0.7) {

            return;
        }

        LOGGER_THREADS.execute(() -> {

            String newLine = Utilities.OS.geLineSeparator();

            for (LogData cachedLog : PENDING_LOGS) {

                //System.out.println(crashLog);

                String fileNameAndExtension = cachedLog.section + "-" + System.currentTimeMillis() + ".log";

                String backedLog = "";

                if (cachedLog.log != null) {

                    backedLog += "LOG : " + newLine +
                            newLine + cachedLog.log;
                }


                if (cachedLog.payload != null) {

                    backedLog += newLine +
                            newLine + "PAYLOAD : " + newLine +
                            newLine + cachedLog.payload;
                }

                BaseResources.getInstance().fileManager.saveFile(BaseFileManager.SECTION_LOG_ERRORS, fileNameAndExtension, backedLog.getBytes());
            }
        });*/
    }
}