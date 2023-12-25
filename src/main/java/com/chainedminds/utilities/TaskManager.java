package com.chainedminds.utilities;

import com.chainedminds.BaseConfig;
import com.chainedminds.BaseMonitor;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskManager {

    private static final String TAG = TaskManager.class.getSimpleName();

    private static final String COLUMN_TASK_NAME = "Name";
    private static final String COLUMN_TASK_TIME = "DateTime";
    private static final List<Task> TASKS = new ArrayList<>();

    private static final ExecutorService TASK_EXECUTOR = Executors.newCachedThreadPool();

    public static void start() {

        new Thread(TaskManager::run).start();
    }

    public static int randomSecond() {

        return new Random().nextInt(60);
    }

    public static void addTask(Task task) {

        if (!task.finalized) {

            return;
        }

        calibrateTime(task);

        addInterval(task);

        if (task.runNow) {

            startTask(task);
        }

        if (task.runAsyncNow) {

            startTaskAsync(task);
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
    }

    private static void startTask(Task task) {

        if (task.saveRecord) {

            TASK_EXECUTOR.execute(() -> saveTaskInvokeTime(task));
        }

        if (task.timingListener != null) {

            task.timingListener.onStartedTask(task);
        }
    }

    private static void startTaskAsync(Task task) {

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

    private static void run() {

        long currentTimeMillis;

        while (BaseMonitor.isAppRunning()) {

            currentTimeMillis = System.currentTimeMillis();

            for (int index = TASKS.size() - 1; index >= 0; index--) {

                Task task = TASKS.get(index);

                if (currentTimeMillis > task.nextRun) {

                    addInterval(task);

                    startTaskAsync(task);
                }
            }

            Utilities.sleep(50);
        }
    }

    public interface TimingListener {

        void onStartedTask(Task task);
    }

    public static class Task {

        private String name;

        private long time;

        private long nextRun;
        private long interval;

        private TimingListener timingListener;

        private boolean runNow;
        private boolean runAsyncNow;
        private boolean saveRecord = false;
        private boolean finalized = false;

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

            this.interval = (day * 24 * 60 * 60 * 1000L) + (hour * 60 * 60 * 1000L) +
                    (minute * 60 * 1000L) + (second * 1000L);

            this.time = calendar.getTimeInMillis();

            return this;
        }

        public Task setName(String name) {

            this.name = name;

            return this;
        }

        public Task saveRecord(boolean saveRecord) {

            this.saveRecord = saveRecord;

            return this;
        }

        @Deprecated
        public Task startAndSchedule() {

            this.runNow = true;
            finalized = true;

            return this;
        }

        @Deprecated
        public Task startAsyncAndSchedule() {

            this.runAsyncNow = true;
            finalized = true;

            return this;
        }

        public Task runNow() {

            this.runNow = true;
            this.runAsyncNow = false;

            return this;
        }

        public Task runAsyncNow() {

            this.runNow = false;
            this.runAsyncNow = true;

            return this;
        }

        public Task schedule() {

            finalized = true;
            return this;
        }

        public Task setTimingListener(TimingListener listener) {

            this.timingListener = listener;

            return this;
        }
    }
}