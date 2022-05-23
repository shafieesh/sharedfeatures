package com.chainedminds.models;

public class BaseContentData {

    @Deprecated
    public String id;
    @SuppressWarnings("unused")
    public int type;
    @SuppressWarnings("unused")
    public String text;
    @SuppressWarnings("unused")
    public String link;
    @Deprecated
    public byte[] blob;
    @Deprecated
    public String fileName;

    public String base64;
}