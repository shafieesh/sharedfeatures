package com.chainedminds.dataClasses;

import java.util.Map;
import java.util.Objects;

public class Asset extends BaseFileData {

    public String appName;
    public String permission;
    public String format;

    public float price;
    public boolean owned;

    public Map<String, String> metaData;

    @Override
    public int hashCode() {

        return Objects.hash(appName, name, type, permission);
    }

    @Override
    public boolean equals(Object anotherObject) {

        if (anotherObject instanceof String) {

            return name.equals(anotherObject);
        }

        if (!(anotherObject instanceof Asset)) {

            return false;
        }

        if (anotherObject == this) {

            return true;
        }

        Asset anotherAsset = (Asset) anotherObject;

        return this.appName.equals(anotherAsset.appName) &&
                this.name.equals(anotherAsset.name) &&
                this.type.equals(anotherAsset.type) &&
                this.format.equals(anotherAsset.format) &&
                this.permission.equals(anotherAsset.permission);
    }
}