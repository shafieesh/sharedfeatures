package com.chainedminds.dataClasses;

import com.chainedminds.BaseCodes;

import java.util.Objects;

public class BackgroundGradient {

    public int id = BaseCodes.UNDEFINED;
    public String type;
    public String[] colors;
    public String orientation;

    @Override
    public int hashCode() {

        return Objects.hash(id, type);
    }

    @Override
    public boolean equals(Object anotherObject) {

        if (anotherObject instanceof Integer) {

            return id == (int) anotherObject;
        }

        if (!(anotherObject instanceof BackgroundGradient)) {

            return false;
        }

        if (anotherObject == this) {

            return true;
        }

        BackgroundGradient anotherAsset = (BackgroundGradient) anotherObject;

        return this.id == anotherAsset.id;
    }
}