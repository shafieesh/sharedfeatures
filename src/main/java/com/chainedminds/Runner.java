package com.chainedminds;

import com.chainedminds.network.netty.NettyServer;

class Runner {

    public static void main(String[] args) {

        TestResources.getInstance().config();

        BaseConfig.setDebugMode(false);

        NettyServer.start();
    }
}