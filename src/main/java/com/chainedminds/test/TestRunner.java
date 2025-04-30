package com.chainedminds.test;

import com.chainedminds._Config;
import com.chainedminds.network.netty.NettyServer;

class TestRunner {

    public static void main(String[] args) {

        _Config.config();
        TestResources.getInstance().config();

        _Config.ENGINE_NAME = "SharedFeatures";
        _Config.SERVER_PORT_TELNET = 23;
        _Config.SERVER_PORT_MAIN = 4295;

        NettyServer.start(false);
    }
}