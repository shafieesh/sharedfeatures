package com.chainedminds.test;

import com.chainedminds._Config;
import com.chainedminds.network.netty.NettyServer;

class TestRunner {

    public static void main(String[] args) {

        _Config.config();
        TestResources.config();

        _Config.PORT_MAIN_FILE_TRANSPORT = 1000;

        _Config.ENGINE_NAME = "SharedFeatures";

        NettyServer.startFileTransport(true);
    }
}