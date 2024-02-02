package com.chainedminds.test;

import com.chainedminds._Config;

class Runner {

    public static void main(String[] args) {

        _Config.config();
        Resources.getInstance().config();
    }
}