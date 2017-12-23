package com.boombike.util;

import java.math.BigInteger;

/**
 * Created by Ravy on 2017. 12. 16..
 */

public class HexUtils {

    private final static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;

        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static String intToHexString(int value) {
        String result = Integer.toHexString(value);
        return result.toUpperCase();
    }

    public static int hexStringToInt(String value) {
        return Integer.parseInt(value, 16);
    }

    public static int byteArrayToInt(byte[] crcByteArr) {
        return new BigInteger(1 , crcByteArr).intValue();
    }
}