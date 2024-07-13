package com.chainedminds.utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Hash {

    private static final String TAG = Hash.class.getSimpleName();

    /*private static byte[] longToBytes(long l) {

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
    }*/

    public static String md5(String input) {

        return md5(input.getBytes());
    }

    public static String md5Unsafe(String input) throws NoSuchAlgorithmException {

        return md5Unsafe(input.getBytes());
    }

    public static String md5(byte[] givenBytes) {

        byte[] md5Bytes = md5Bytes(givenBytes);

        if (md5Bytes != null) {

            return getHex(md5Bytes);
        }

        return null;
    }

    public static String md5Unsafe(byte[] givenBytes) throws NoSuchAlgorithmException {

        byte[] md5Bytes = md5BytesUnsafe(givenBytes);

        if (md5Bytes != null) {

            return getHex(md5Bytes);
        }

        return null;
    }

    public static byte[] md5Bytes(byte[] givenBytes) {

        try {

            return md5BytesUnsafe(givenBytes);

        } catch (Exception e) {

            return null;
        }
    }

    public static byte[] md5BytesUnsafe(byte[] givenBytes) throws NoSuchAlgorithmException {

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        messageDigest.update(givenBytes);

        return messageDigest.digest();
    }

    private static String getHex(final byte[] md5Bytes) {

        if (md5Bytes != null) {

            Formatter formatter = new Formatter();

            for (byte b : md5Bytes) {

                formatter.format("%02x", b);
            }

            String result = formatter.toString();

            formatter.close();

            return result;
        }

        return null;
    }
}
