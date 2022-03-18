package com.chainedminds.utilities;

import com.chainedminds.BaseConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.Lock;

public class Utilities {

    public static void sleep(long millis) {

        try {

            Thread.sleep(millis);

        } catch (Exception ignore) {

        }
    }

    public static boolean isNumber(String input) {

        try {

            Long.parseLong(input);

            return true;

        } catch (Exception ignored) {

            return false;
        }
    }

    public static String getTimeLeftTill(long time, String language) {

        String result = "";

        long diff = time - System.currentTimeMillis();

        if (diff < 0) return result;

        if (diff > BaseConfig.ONE_DAY) {

            if ("FA".equals(language)) {

                result = Math.round(diff / BaseConfig.ONE_DAY) + " روز";

            } else {

                result = Math.round(diff / BaseConfig.ONE_DAY) + " days";
            }
        } else if (diff > BaseConfig.ONE_HOUR) {

            if ("FA".equals(language)) {

                result = Math.round(diff / BaseConfig.ONE_HOUR) + " ساعت";

            } else {

                result = Math.round(diff / BaseConfig.ONE_HOUR) + " hours";
            }
        } else if (diff > BaseConfig.ONE_MINUTE) {

            if ("FA".equals(language)) {

                result = Math.round(diff / BaseConfig.ONE_MINUTE) + " دقیقه";

            } else {

                result = Math.round(diff / BaseConfig.ONE_MINUTE) + " minutes";
            }
        }

        return localizeNumbers(result, language);
    }

    public static String replaceLocalizedNumbers(String input) {

        return input.replaceAll("۰", "0")
                .replaceAll("۱", "1")
                .replaceAll("۲", "2")
                .replaceAll("۳", "3")
                .replaceAll("۴", "4")
                .replaceAll("۵", "5")
                .replaceAll("۶", "6")
                .replaceAll("۷", "7")
                .replaceAll("۸", "8")
                .replaceAll("۹", "9");
    }

    public static boolean hasLocalizedNumbers(String input) {

        return input.contains("۰")
                || input.contains("۱")
                || input.contains("۲")
                || input.contains("۳")
                || input.contains("۴")
                || input.contains("۵")
                || input.contains("۶")
                || input.contains("۷")
                || input.contains("۸")
                || input.contains("۹");
    }

    public static String localizeNumbers(long input, String language) {

        return localizeNumbers(input + "", language);
    }

    public static String localizeNumbers(String input, String language) {

        language = language.toUpperCase();

        if (!"FA".equals(language)) {

            return input;
        }

        return input.replaceAll("0", "۰")
                .replaceAll("1", "۱")
                .replaceAll("2", "۲")
                .replaceAll("3", "۳")
                .replaceAll("4", "۴")
                .replaceAll("5", "۵")
                .replaceAll("6", "۶")
                .replaceAll("7", "۷")
                .replaceAll("8", "۸")
                .replaceAll("9", "۹");
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap) {

        List<Map.Entry<K, V>> list = new LinkedList<>(unsortMap.entrySet());

        list.sort(Comparator.comparing(o -> (o.getValue())));

        Map<K, V> result = new LinkedHashMap<>();

        for (Map.Entry<K, V> entry : list) {

            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Internal helper method to handle the communication to the server.
     *
     * @param url     The url of the API to connect in to.
     * @param headers The request headers to be sent.
     * @return true if the connection was successful, false otherwise.
     */
    public static boolean openConnection(String url, Map<String, String> headers, String requestMethod, String payload, HttpResponseCallback httpResponseCallback) {

        boolean wasSuccessful = false;

        try {

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            if (headers != null) {

                for (String key : headers.keySet()) {

                    connection.setRequestProperty(key, headers.get(key));
                }
            }

            if (requestMethod != null) {
                connection.setRequestMethod(requestMethod);
                connection.setDoOutput(true);
            }

            connection.connect();

            if (payload != null) {

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(payload.getBytes());
                outputStream.close();
            }

            InputStream inputStream;

            int httpResponseCode = connection.getResponseCode();

            if (httpResponseCode >= 200 && httpResponseCode < 300) {

                wasSuccessful = true;

                inputStream = connection.getInputStream();

            } else {

                inputStream = connection.getErrorStream();
            }

            String responseMessage = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

            inputStream.close();

            connection.disconnect();

            if (httpResponseCallback != null) {

                httpResponseCallback.onHttpResponse(httpResponseCode, responseMessage);
            }

        } catch (IOException e) {

            e.printStackTrace();

            Log.error("HttpConnection", e);
        }

        return wasSuccessful;
    }

    public static boolean openConnection(String url, Map<String, String> headers, HttpResponseCallback httpResponseCallback, boolean useProxy) {

        boolean wasSuccessful = false;

        try {

            HttpURLConnection connection;

            if (useProxy) {

                Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("vpn.fandoghapps.com", 65001));

                connection = (HttpURLConnection) new URL(url).openConnection(proxy);

            } else {

                connection = (HttpURLConnection) new URL(url).openConnection();
            }

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            if (headers != null) {

                for (String key : headers.keySet()) {

                    connection.setRequestProperty(key, headers.get(key));
                }
            }

            connection.connect();

            InputStream inputStream;

            int httpResponseCode = connection.getResponseCode();

            if (httpResponseCode == 200) {

                wasSuccessful = true;

                inputStream = connection.getInputStream();

            } else {

                inputStream = connection.getErrorStream();
            }

            String responseMessage = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

            inputStream.close();

            connection.disconnect();

            if (httpResponseCallback != null) {

                httpResponseCallback.onHttpResponse(httpResponseCode, responseMessage);
            }

        } catch (IOException e) {

            e.printStackTrace();

            Log.error("HttpConnection", e);
        }

        return wasSuccessful;
    }

    /**
     * Helper method for doing a full try catch in a nested method.<br>
     * This will also catches any uncaught exceptions and saves the exception with {@link Log} utility.
     *
     * @param Tag The class name which is requesting this try-catch. This will be used when logging the exceptions.
     * @param job The nested job that needs to be completed inside a try-catch.
     */
    public static void tryAndCatch(String Tag, Job job) {

        try {

            job.doJob();

        } catch (Exception e) {

            Log.error(Tag, e);
        }
    }

    public static void tryAndIgnore(Job job) {

        try {

            job.doJob();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static void lock(String Tag, Lock lock, Job job) {

        try {

            lock.lock();

            job.doJob();

        } catch (Exception e) {

            Log.error(Tag, e);

        } finally {

            lock.unlock();
        }
    }

    public static void retryWithin(long milliseconds, Job job) {

        new Thread(() -> {

            Utilities.sleep(milliseconds);

            if (job != null) {

                try {

                    job.doJob();

                } catch (Exception ignore) {

                }
            }

        }).start();
    }

    @Deprecated
    public static boolean nullCheck(Object... objects) {

        if (objects != null) {

            for (Object object : objects) {

                if (object == null) {

                    return true;
                }
            }
        }

        return false;
    }

    public static class Pagination {

        public static <T> void getRange(List<T> contents, int offset, int limit, Callback<T> callback) {

            int counter = 0;
            int filled = 0;

            for (int index = contents.size() - 1; index >= 0; index--) {

                T content = contents.get(index);

                if (callback.isOk(content)) {

                    counter++;

                    if (counter <= offset) {

                        continue;
                    }

                    if (filled < limit) {

                        callback.addItem(content);
                    }
                }
            }

            callback.totalCount(counter);
        }

        public interface Callback<T> {

            boolean isOk(T t);

            void addItem(T t);

            void totalCount(int count);
        }
    }

    public interface Job {

        void doJob() throws Exception;
    }

    public interface GrantAccess<T> {

        void giveAccess(T t) throws Exception;
    }

    public static class OS {

        enum OS_TYPE {

            GENERIC, WINDOWS, LINUX, MAC
        }

        private static OS_TYPE os_type = null;
        private static String home_directory = null;
        private static String working_directory = null;

        public static OS_TYPE getType() {

            if (os_type == null) {

                String os = System.getProperty("os.name", "generic").toLowerCase();

                if (os.startsWith("windows")) {

                    os_type = OS_TYPE.WINDOWS;

                } else if (os.startsWith("linux")) {

                    os_type = OS_TYPE.LINUX;

                } else if (os.startsWith("mac") || os.startsWith("darwin")) {

                    os_type = OS_TYPE.MAC;

                } else {

                    os_type = OS_TYPE.GENERIC;
                }
            }

            return os_type;
        }

        public static String getHomeDirectory() {

            if (home_directory == null) {

                if (getType() == OS_TYPE.WINDOWS) {

                    home_directory = "C:" + getFileSeparator();

                } else {

                    home_directory = System.getProperty("user.home") + getFileSeparator();

                    if (home_directory.contains("root")) {

                        //home_directory = home_directory.replace("root", "ubuntu");
                    }
                }
            }

            return home_directory;
        }

        public static String getWorkingDirectory() {

            if (working_directory == null) {

                if (getType() == OS_TYPE.WINDOWS) {

                    working_directory = "C:" + getFileSeparator();

                } else {

                    working_directory = System.getProperty("user.dir") + getFileSeparator();

                    /*if (working_directory.contains("root")) {

                        working_directory = working_directory.replace("root", "ubuntu");
                    }*/
                }
            }

            return working_directory;
        }

        public static String geLineSeparator() {

            return System.lineSeparator();
        }

        public static char getFileSeparator() {

            return java.io.File.separatorChar;
        }
    }

    public static class File {

        public static boolean write(String filePath, byte[] bytes) {

            try {

                FileOutputStream outputStream = new FileOutputStream(filePath);
                outputStream.write(bytes);
                outputStream.close();

                return true;

            } catch (Exception e) {

                e.printStackTrace();
            }

            return false;
        }
    }

    public static class Error {

        public static String toString(Throwable error) {

            StringWriter stringWriter = new StringWriter();

            error.printStackTrace(new PrintWriter(stringWriter));

            return stringWriter.toString();
        }
    }
}