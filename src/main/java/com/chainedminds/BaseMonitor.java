package com.chainedminds;

import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.network.netty.NettyServer;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database.DatabaseHelper;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class BaseMonitor extends Thread {

    private static final OperatingSystemMXBean OS_MX_BEAN = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    private static boolean isAppRunning = true;
    private static int counter = 0;

    private static final Map<String, Object> system = new HashMap<>();
    private static Map<String, Integer> database;
    private static Map<String, Integer> network;

    public static Map<String, Object> getSystemStatus() {

        Map<String, Object> status = new HashMap<>();
        status.put("system", system);
        status.put("network", network);
        status.put("database", database);

        return status;
    }

    public static BaseData forceCloseServer(BaseData data) {

        String password = data.account.password;
        int userID = data.account.id;

        data = new BaseData();
        data.response = BaseConfig.RESPONSE_NOK;

        if ("B7R538QT387XRBO2R78XR2837".equals(password) && 1241234 == userID) {

            isAppRunning = false;

            System.out.println("\n#################################");
            System.out.println("#################################\n");

            System.out.println("  SELF DESTRUCTING GAME SERVER");

            System.out.println("\n#################################");
            System.out.println("#################################\n");

            data.response = BaseConfig.RESPONSE_OK;
        }

        return data;
    }

    public static boolean isAppRunning() {

        return isAppRunning;
    }

    public static float getCpuUsage() {

        return (float) system.getOrDefault("cpu", 0f);
    }

    @Override
    public void run() {

        while (isAppRunning()) {

            try {

                float cpu = (float) ((int) (100 * OS_MX_BEAN.getSystemCpuLoad())) / 100f;
                int threads = ManagementFactory.getThreadMXBean().getThreadCount();

                Runtime runtime = Runtime.getRuntime();
                int mb = 1024 * 1024;

                long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / mb;
                long allocatedMemory = runtime.totalMemory() / mb;
                long maxMemory = runtime.maxMemory() / mb;
                long availableMemory = maxMemory - usedMemory;

                system.put("cpu", cpu);
                system.put("threads", threads);
                system.put("memory", usedMemory);
                system.put("availableMemory", availableMemory);

                counter++;

                if (counter >= 20) {

                    counter = 0;

                    database = DatabaseHelper.getConnectionsCount();
                    network = NettyServer.getConnectionsCount();

                    StringBuilder stringBuilder = new StringBuilder();

                    stringBuilder.append("\n");
                    stringBuilder.append("\n");
                    stringBuilder.append("----------------BACKEND MONITOR-----------------\n");
                    stringBuilder.append("\n");
                    stringBuilder.append("--------- " + new Timestamp(System.currentTimeMillis()).toString() + "\n");
                    stringBuilder.append("\n");
                    stringBuilder.append("--------- System\n");
                    stringBuilder.append("\n");
                    stringBuilder.append("- CPU : " + cpu + "\n");
                    stringBuilder.append("- Threads : " + threads + "\n");
                    stringBuilder.append("- Memory : " + usedMemory + "\n");
                    stringBuilder.append("- Available Memory : " + availableMemory + "\n");
                    //stringBuilder.append("Allocated Memory : " + allocatedMemory + "\n");
                    //stringBuilder.append("Max Memory : " + maxMemory + " mb" + "\n");

                    stringBuilder.append("\n");
                    stringBuilder.append("--------- Database\n");
                    stringBuilder.append("\n");
                    stringBuilder.append("- Queries : " + database.get("queries") + "\n");
                    stringBuilder.append("- Updates : " + database.get("updates") + "\n");
                    stringBuilder.append("- Inserts : " + database.get("inserts") + "\n");
                    stringBuilder.append("- Total : " + database.get("total") + "\n");

                    stringBuilder.append("\n");
                    stringBuilder.append("--------- Network\n");
                    stringBuilder.append("\n");

                    stringBuilder.append("- MainPipe New Connections : " + network.get("mainPipe") + "\n");
                    stringBuilder.append("- TelnetPipe New Connections : " + network.get("telnetPipe") + "\n");
                    stringBuilder.append("- WebPipe New Connections : " + network.get("webPipe") + "\n");
                    stringBuilder.append("- Total New Connections : " + network.get("total") + "\n");
                    stringBuilder.append("- Active Connections : " + network.get("active") + "\n");

                    //System.out.println("--------- Lobbies");
                    //System.out.println();
                    //System.out.println("Dots & Boxes : " + DB_V_Lobby.LOBBIES.size());

                    /*for (int count : popularRequests.keySet()) {

                        //System.out.println(popularRequests.get(count) + " --> " + count);
                    }*/

                    stringBuilder.append("\n");
                    stringBuilder.append("------------------------------------------------\n");
                    stringBuilder.append("\n");
                    stringBuilder.append("\n");

                    System.out.println(stringBuilder.toString());

                    System.gc();
                }

                Utilities.sleep(500);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}
