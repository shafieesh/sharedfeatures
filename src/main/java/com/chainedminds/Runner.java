package com.chainedminds;

import com.chainedminds.network.netty.NettyServer;
import com.chainedminds.utilities.CacheManager;
import com.chainedminds.utilities.Utilities;

class Runner {

    private static final CacheManager<String, Integer> LOGIN_ATTEMPTS_CACHE = new CacheManager<>(5000);

    public static void main(String[] args) {

        TestResources.getInstance().config();

        BaseConfig.setDebugMode(false);

        NettyServer.start();

        LOGIN_ATTEMPTS_CACHE.put("aa", 1);

        new Thread(() -> {

            Utilities.sleep(10_000);

            System.out.println();

        }).start();
    }
}