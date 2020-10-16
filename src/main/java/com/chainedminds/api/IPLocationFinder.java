package com.chainedminds.api;

import com.chainedminds.utilities.TaskManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IPLocationFinder {

    public static final String COUNTRY_IR = "IR";
    public static final String NOT_AVAILABLE = "--";

    private static final AtomicInteger counter = new AtomicInteger();

    private static final Map<String, String> IP_ADDRESSES = new HashMap<>();

    public static void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("CleanUpIPCache")
                .setTime(0, 0, 0)
                .setInterval(1, 0, 0, 0)
                .setTimingListener(task -> {

                    Calendar calendar = Calendar.getInstance();

                    if (calendar.get(Calendar.DAY_OF_WEEK) % 2 == 1) {

                        IP_ADDRESSES.clear();
                    }
                }));

        TaskManager.addTask(TaskManager.Task.build()
                .setName("IPFinderCounterReset")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 1, 0)
                .setTimingListener(task -> {

                    counter.set(0);
                }));
    }

    public static String getCountry(String ipAddress) {

        return NOT_AVAILABLE;

        /*if (IP_ADDRESSES.containsKey(ipAddress)) {

            return IP_ADDRESSES.get(ipAddress);

        } else {

            counter.incrementAndGet();

            if (counter.get() >= 150) {

                return NOT_AVAILABLE;
            }

            String jsonString;

            jsonString = DataTransportManager.httpGet("http://ip-api.com/json/" + ipAddress);

            IPApiData ipApiData = JsonHelper.getObject(jsonString, IPApiData.class);

            if (ipApiData != null && ipApiData.countryCode != null) {

                IP_ADDRESSES.put(ipAddress, ipApiData.countryCode);

                return ipApiData.countryCode;
            }

            jsonString = DataTransportManager.httpGet("http://api.ipstack.com/" +
                    ipAddress + "?access_key=02755c672bb07ebc6aeb1baf235ad835&format=1");

            IPStackData ipStackData = JsonHelper.getObject(jsonString, IPStackData.class);

            if (ipStackData != null && ipStackData.country_code != null) {

                IP_ADDRESSES.put(ipAddress, ipStackData.country_code);

                return ipStackData.country_code;
            }

            return NOT_AVAILABLE;
        }*/
    }
}