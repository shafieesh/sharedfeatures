package com.chainedminds.models.account;

public class _FriendData extends _AccountData {

    public int status;

    @Override
    public int hashCode() {

        return super.id;
    }

    @Override
    public boolean equals(Object anotherObject) {

        if (anotherObject instanceof Number) {

            return super.id == (int) anotherObject;
        }

        if (!(anotherObject instanceof _FriendData)) {

            return false;
        }

        if (anotherObject == this) {

            return true;
        }

        _FriendData anotherFriend = (_FriendData) anotherObject;

        return super.id == anotherFriend.id;
    }
}