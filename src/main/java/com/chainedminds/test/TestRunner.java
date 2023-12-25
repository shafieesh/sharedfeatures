package com.chainedminds.test;

import com.chainedminds.BaseConfig;

class TestRunner {

    public static void main(String[] args) {

        TestResources.getInstance().config();

        BaseConfig.setDebugMode(false);
    }
}