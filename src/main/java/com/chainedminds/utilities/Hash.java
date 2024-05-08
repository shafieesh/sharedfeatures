package com.chainedminds.utilities;

import java.security.MessageDigest;
import java.util.Formatter;

public class Hash {

    private static final String TAG = Hash.class.getSimpleName();

    private static byte[] longToBytes(long l) {

        byte[] result = new byte[8];

        for (int i = 7; i >= 0; i--) {

            result[i] = (byte) (l & 0xFF);

            l >>= 8;
        }

        return result;
    }

    public static long bytesToLong(byte[] b) {

        long result = 0;

        for (int i = 0; i < 8; i++) {

            result <<= 8;

            result |= (b[i] & 0xFF);
        }

        return result;
    }

    public static String md5(long givenLong) {

        String hash = null;

        try {

            byte[] givenBytes = longToBytes(givenLong);

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.update(givenBytes);

            hash = byteToHex(messageDigest.digest());

        } catch (Exception e) {

            _Log.error(TAG, e);
        }

        return hash;
    }

    public static String md5(String input) {

        String hash = null;

        try {

            byte[] givenBytes = input.getBytes();

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.update(givenBytes);

            hash = byteToHex(messageDigest.digest());

        } catch (Exception e) {

            _Log.error(TAG, e);
        }

        return hash;
    }

    private static String byteToHex(final byte[] hash) {

        Formatter formatter = new Formatter();

        for (byte b : hash) {

            formatter.format("%02x", b);
        }

        String result = formatter.toString();

        formatter.close();

        return result;
    }
}
