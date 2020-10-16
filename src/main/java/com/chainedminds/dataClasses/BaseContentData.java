package com.chainedminds.dataClasses;

import jdk.jfr.Unsigned;

public class BaseContentData {

    @Deprecated
    @Unsigned
    public String id;
    @SuppressWarnings("unused")
    public int type;
    @SuppressWarnings("unused")
    public String text;
    @SuppressWarnings("unused")
    public String link;
    @Deprecated
    @Unsigned
    public byte[] blob;
    @Deprecated
    @Unsigned
    public String fileName;
}