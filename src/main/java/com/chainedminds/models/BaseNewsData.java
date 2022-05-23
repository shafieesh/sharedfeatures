package com.chainedminds.models;

import java.util.List;

public class BaseNewsData {

    public int id;
    public String title;
    public String summary;
    public List<BaseContentData> contents;
    public BaseContentData content;
    public long publishDate;
    public String banner;
    public String bannerLink;
    public String coverLink;
    public String language;
    public String appName;
    public int count;
}