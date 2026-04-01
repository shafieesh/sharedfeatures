package com.chainedminds.models;

import com.chainedminds._Config;

@SuppressWarnings("unused")
public class SmallData {

    public Integer request;
    public Integer subRequest;
    public Integer response;
    public String message;

    public String engine = _Config.ENGINE_NAME;
}