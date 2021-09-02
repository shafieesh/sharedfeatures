package com.chainedminds.utilities;

import com.chainedminds.BaseConfig;
import com.chainedminds.BaseMonitor;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskManager extends Thread {

    private static final String TAG = TaskManager.class.getSimpleName();

    private static final int TWO_SECONDS = 1500;

    private static final String COLUMN_TASK_NAME = "Name";
    private static final String COLUMN_TASK_TIME = "DateTime";
    private static final List<Task> TASKS = new ArrayList<>();

    private static final ExecutorService TASK_EXECUTOR = Executors.newCachedThreadPool();

    public static int randomSecond() {

        return new Random().nextInt(60);
    }

    public static void addTask(Task task) {

        calibrateTime(task);

        if (!task.executedBefore) {

            task.executedBefore = getExecutedBefore(task);

            if (task.executedBefore) addInterval(task);
        }

        long currentTime = System.currentTimeMillis();

        if (task.startAndSchedule) {

            addInterval(task);

            startTask(task, currentTime);

        } else if (task.startAsyncAndSchedule || !task.executedBefore) {

            addInterval(task);

            startTaskAsync(task, currentTime);
        }

        TASKS.add(task);
    }

    private static void calibrateTime(Task task) {

        long currentTime = System.currentTimeMillis();

        while ((currentTime - task.time) >= task.interval) {
            addInterval(task);
        }
    }

    private static void addInterval(Task task) {

        task.time += task.interval;
        task.nextRun = task.time;

        /*task.calendar.setTimeInMillis(task.time);

        task.calendar.add(Calendar.DAY_OF_YEAR, task.dayInterval);
        task.calendar.add(Calendar.HOUR_OF_DAY, task.hourInterval);
        task.calendar.add(Calendar.MINUTE, task.minuteInterval);
        task.calendar.add(Calendar.SECOND, task.secondInterval);

        task.time = task.calendar.getTimeInMillis();*/
    }

    private static boolean getExecutedBefore(Task task) {

        AtomicBoolean hasBeenDoneInCurrentInterval = new AtomicBoolean(false);

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_TASKS_HISTORY + " WHERE "
                + COLUMN_TASK_NAME + " = ? AND " + COLUMN_TASK_TIME + " >= ?";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, task.name);
        parameters.put(2, new Timestamp(task.time));

        BaseDatabaseHelperOld.query(TAG, selectStatement, parameters, resultSet -> {

            if (resultSet.next()) {

                hasBeenDoneInCurrentInterval.set(true);
            }
        });

        return hasBeenDoneInCurrentInterval.get();
    }

    private static void startTask(Task task, long taskInvokeTime) {

        task.lastRun = taskInvokeTime;

        if (task.saveRecord) {

            saveTaskInvokeTime(task);
        }

        if (task.timingListener != null) {

            task.timingListener.onStartedTask(task);
        }
    }

    private static void startTaskAsync(Task task, long taskInvokeTime) {

        task.lastRun = taskInvokeTime;

        if (task.saveRecord) {

            TASK_EXECUTOR.execute(() -> saveTaskInvokeTime(task));
        }

        if (task.timingListener != null) {

            TASK_EXECUTOR.execute(() -> task.timingListener.onStartedTask(task));
        }
    }

    private static void saveTaskInvokeTime(Task task) {

        String insertStatement = "INSERT " + BaseConfig.TABLE_TASKS_HISTORY + " (" +
                COLUMN_TASK_NAME + ", " + COLUMN_TASK_TIME + ") " + "VALUES (?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, task.name);
        parameters.put(2, new Timestamp(System.currentTimeMillis()));

        BaseDatabaseHelperOld.insert(TAG, insertStatement, parameters);
    }

    @Override
    public void run() {

        long currentTimeMillis;

        while (BaseMonitor.isAppRunning()) {

            currentTimeMillis = System.currentTimeMillis();

            for (int index = TASKS.size() - 1; index >= 0; index--) {

                Task task = TASKS.get(index);

                if (currentTimeMillis > task.nextRun) {

                    addInterval(task);

                    startTaskAsync(task, currentTimeMillis);
                }
            }

            Utilities.sleep(50);
        }
    }

    public interface TimingListener {

        void onStartedTask(Task task);
    }

    public interface SearchListener {

        void onFoundTask(Task task);
    }

    public static class Task {

        private String name;

        private long time;

        private long lastRun;
        private long nextRun;
        private long interval;

        private TimingListener timingListener;

        private boolean startAndSchedule;
        private boolean executedBefore;
        private boolean startAsyncAndSchedule;
        private boolean saveRecord = false;

        public static Task build() {

            Task task = new Task();
            task.time = System.currentTimeMillis();

            return task;
        }

        public Task setTime(int hour, int minute, int second) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(this.time);

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);
            calendar.set(Calendar.MILLISECOND, 0);

            this.time = calendar.getTimeInMillis();

            return this;
        }

        public Task setInterval(int day, int hour, int minute, int second) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(this.time);

            calendar.add(Calendar.DAY_OF_YEAR, -day);
            calendar.add(Calendar.HOUR_OF_DAY, -hour);
            calendar.add(Calendar.MINUTE, -minute);
            calendar.add(Calendar.SECOND, -second);

            this.interval = (day * 24 * 60 * 60 * 1000) + (hour * 60 * 60 * 1000) +
                    (minute * 60 * 1000) + (second * 1000);

            this.time = calendar.getTimeInMillis();

            return this;
        }

        public Task setName(String name) {

            this.name = name;

            return this;
        }

        public Task setExecutedBefore(boolean state) {

            this.executedBefore = state;

            return this;
        }

        public Task saveRecord(boolean saveRecord) {

            this.saveRecord = saveRecord;

            return this;
        }

        public Task startAndSchedule() {

            this.executedBefore = true;
            this.startAndSchedule = true;

            return this;
        }

        public Task startAsyncAndSchedule() {

            this.executedBefore = true;
            this.startAsyncAndSchedule = true;

            return this;
        }

        public Task setTimingListener(TimingListener listener) {

            this.timingListener = listener;

            return this;
        }
    }
}