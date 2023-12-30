package com.chainedminds.utilities;

import com.chainedminds._Config;
import com.chainedminds._Monitor;
import com.chainedminds.utilities.database._DatabaseOld;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Task {

    private static final String TAG = Task.class.getSimpleName();

    private static final String COLUMN_TASK_NAME = "Name";
    private static final String COLUMN_TASK_TIME = "DateTime";
    private static final List<Data> TASKS = new ArrayList<>();

    private static final ExecutorService TASK_EXECUTOR = Executors.newCachedThreadPool();

    public static void start() {

        new Thread(Task::run).start();
    }

    public static int randomSecond() {

        return new Random().nextInt(60);
    }

    public static void add(Data task) {

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

    private static void calibrateTime(Data task) {

        long currentTime = System.currentTimeMillis();

        while ((currentTime - task.time) >= task.interval) {

            addInterval(task);
        }
    }

    private static void addInterval(Data task) {

        task.time += task.interval;
        task.nextRun = task.time;
    }

    private static void startTask(Data task) {

        if (task.saveRecord) {

            TASK_EXECUTOR.execute(() -> saveTaskInvokeTime(task));
        }

        if (task.timingListener != null) {

            task.timingListener.onStartedTask(task);
        }
    }

    private static void startTaskAsync(Data task) {

        if (task.saveRecord) {

            TASK_EXECUTOR.execute(() -> saveTaskInvokeTime(task));
        }

        if (task.timingListener != null) {

            TASK_EXECUTOR.execute(() -> task.timingListener.onStartedTask(task));
        }
    }

    private static void saveTaskInvokeTime(Data task) {

        String insertStatement = "INSERT " + _Config.TABLE_TASKS_HISTORY + " (" +
                COLUMN_TASK_NAME + ", " + COLUMN_TASK_TIME + ") " + "VALUES (?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, task.name);
        parameters.put(2, new Timestamp(System.currentTimeMillis()));

        _DatabaseOld.insert(TAG, insertStatement, parameters);
    }

    private static void run() {

        long currentTimeMillis;

        while (_Monitor.isAppRunning()) {

            currentTimeMillis = System.currentTimeMillis();

            for (int index = TASKS.size() - 1; index >= 0; index--) {

                Data task = TASKS.get(index);

                if (currentTimeMillis > task.nextRun) {

                    addInterval(task);

                    startTaskAsync(task);
                }
            }

            Utilities.sleep(50);
        }
    }

    public interface TimingListener {

        void onStartedTask(Data task);
    }

    public static class Data {

        private String name;

        private long time;

        private long nextRun;
        private long interval;

        private TimingListener timingListener;

        private boolean runNow;
        private boolean runAsyncNow;
        private boolean saveRecord = false;
        private boolean finalized = false;

        public static Data build() {

            Data task = new Data();
            task.time = System.currentTimeMillis();

            return task;
        }

        public Data setTime(int hour, int minute, int second) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(this.time);

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);
            calendar.set(Calendar.MILLISECOND, 0);

            this.time = calendar.getTimeInMillis();

            return this;
        }

        public Data setInterval(int day, int hour, int minute, int second) {

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

        public Data setName(String name) {

            this.name = name;

            return this;
        }

        public Data saveRecord(boolean saveRecord) {

            this.saveRecord = saveRecord;

            return this;
        }

        @Deprecated
        public Data startAndSchedule() {

            this.runNow = true;
            finalized = true;

            return this;
        }

        @Deprecated
        public Data startAsyncAndSchedule() {

            this.runAsyncNow = true;
            finalized = true;

            return this;
        }

        public Data runNow() {

            this.runNow = true;
            this.runAsyncNow = false;

            return this;
        }

        public Data runAsyncNow() {

            this.runNow = false;
            this.runAsyncNow = true;

            return this;
        }

        public Data schedule() {

            finalized = true;
            return this;
        }

        public Data setTimingListener(TimingListener listener) {

            this.timingListener = listener;

            return this;
        }
    }
}