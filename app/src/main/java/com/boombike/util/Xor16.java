package com.boombike.util;

import android.text.TextUtils;

/**
 * Created by Ravy on 2017. 12. 16..
 */

public class Xor16 {

//    public static String changeCode(String paramString1, String paramString2, int paramInt) {
//        int j = paramString1.length() / 2;
//        String[] arrayOfString = new String[j];
//        int i = 0;
//        if (i < j) {
//            if (i == 1) {
//                arrayOfString[i] = Integer.toHexString(Integer.parseInt(paramString2, 16) + Integer.parseInt("32", 16));
//                if (arrayOfString[1].length() > 2) {
//                    arrayOfString[1] = arrayOfString[1].substring(0, arrayOfString[1].length() - 1);
//                }
//            }
//            for (; ; ) {
//                i++;
//                break;
//                arrayOfString[i] = paramString1.substring(i * 2, i * 2 + 2);
//            }
//        }
//        paramString1 = "";
//        i = 0;
//        if (i < arrayOfString.length) {
//            if (i < paramInt) {
//            }
//            for (paramString1 = paramString1 + arrayOfString[i]; ; paramString1 = paramString1 + xor(arrayOfString[i], paramString2)) {
//                i++;
//                break;
//            }
//        }
//        return paramString1;
//    }
//
//    public static byte[] toBytes(String hexText) {
//        if (TextUtils.isEmpty(hexText)) {
//            return new byte[0];
//        }
//
//        return new java.math.BigInteger(hexText, 16).toByteArray();
//    }
//
//    private static String xor(String paramString1, String paramString2) {
//        int i;
//
//        paramString1 = Integer.toBinaryString(Integer.valueOf(paramString1, 16));
//        String str2 = Integer.toBinaryString(Integer.valueOf(paramString2, 16));
//        String str3 = "";
//        paramString2 = paramString1;
//        if (paramString1.length() != 8) {
//            for (i = paramString1.length(); ; i++) {
//                paramString2 = paramString1;
//                if (i >= 8) {
//                    break;
//                }
//                paramString1 = "0" + paramString1;
//            }
//        }
//        String str1 = str2;
//        if (str2.length() != 8) {
//            i = str2.length();
//            paramString1 = str2;
//            for (; ; ) {
//                str1 = paramString1;
//                if (i >= 8) {
//                    break;
//                }
//                paramString1 = "0" + paramString1;
//                i++;
//            }
//        }
//
//        i = 0;
//        paramString1 = str3;
//        if (i < paramString2.length()) {
//            if (str1.charAt(i) == paramString2.charAt(i)) {
//            }
//            for (paramString1 = paramString1 + "0"; ; paramString1 = paramString1 + "1") {
//                i++;
//                break;
//            }
//        }
//        paramString2 = Integer.toHexString(Integer.parseInt(paramString1, 2));
//        if (paramString2.length() == 1) {
//            paramString1 = "0" + paramString2;
//        }
//
//        return paramString1;
//    }
}
