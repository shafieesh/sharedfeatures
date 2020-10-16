package com.chainedminds.utilities;

import java.util.Base64;

public class Base64Helper {

    private static final String TAG = Base64Helper.class.getSimpleName();

    public static byte[] decode(String encodedString) {

        byte[] data = null;

        try {

            encodedString = encodedString
                    .replaceAll("_", "/")
                    .replaceAll("-", "+")
                    .replaceAll(" ", "+");

            data = Base64.getDecoder().decode(encodedString);

        } catch (Exception e) {

            Log.error(TAG, e);
        }

        return data;
    }

    public static boolean validate(String encodedString) {

        try {

            encodedString = encodedString
                    .replaceAll("_", "/")
                    .replaceAll("-", "+")
                    .replaceAll(" ", "+");

            Base64.getDecoder().decode(encodedString);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    public static String encode(byte[] data) {

        String encodedString = null;

        try {

            if (data != null) {

                encodedString = Base64.getEncoder().encodeToString(data);
            }

        } catch (Exception e) {

            Log.error(TAG, e);
        }

        return encodedString;
    }
}
