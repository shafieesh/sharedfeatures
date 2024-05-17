package com.chainedminds.test;

import com.chainedminds._Config;

class TestRunner {

    public static void main(String[] args) {

        _Config.config();
        TestResources.getInstance().config();
    }
}