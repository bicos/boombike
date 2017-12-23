package com.boombike.omni;

import com.boombike.util.CRC16;
import com.boombike.util.HexUtils;

/**
 * Created by Ravy on 2017. 12. 17..
 */

public class OmniUtils {

    public static final byte HEADER = (byte) 0xFE;

    public static final byte ENCRYPT_PLUS_NUMBER = 0x32;

    public static final int INDEX_NUM = 1;

    public static final int INDEX_DATA_START = 2; // include id, key, cmd, len, data(key), crc

    private static final int INDEX_LENGTH = 8;

    public static String toDecryptStr(String hexStr) {
        byte[] cipher = HexUtils.hexStringToByteArray(hexStr);

        // find length value
        byte lengthValue = cipher[INDEX_LENGTH];

        // decode length value
        byte num = (byte) (cipher[INDEX_NUM] - ENCRYPT_PLUS_NUMBER);
        int length = lengthValue ^ num;
        int plainLength = INDEX_LENGTH + length + 1;

        // find crc value
        byte[] crcByteArr = {cipher[INDEX_LENGTH + length + 1], cipher[INDEX_LENGTH + length + 2]};
        int actualCrcValue = HexUtils.byteArrayToInt(crcByteArr);

        byte[] plain = new byte[plainLength];
        System.arraycopy(cipher, 0, plain, 0, plain.length);

        // calculate crc16
        int crc16 = CRC16.calcCrc16(plain);

        // compare crc16
        if (actualCrcValue != crc16) {
            throw new IllegalStateException("crc value not same");
        }

        // NUM XOR AFTER NUM
        byte[] data = new byte[plain.length - INDEX_DATA_START];
        byte[] newDataByteArr = new byte[plain.length - INDEX_DATA_START];
        System.arraycopy(plain, INDEX_DATA_START, data, 0, data.length);
        for (int i = 0; i < data.length; i++) {
            newDataByteArr[i] = (byte) (plain[i + INDEX_DATA_START] ^ num);
        }

        // num data write
        plain[INDEX_NUM] = num;
        // xor data write
        System.arraycopy(newDataByteArr, 0, plain, INDEX_DATA_START, newDataByteArr.length);

        return HexUtils.byteArrayToHexString(plain);
    }

    public static String toEncryptStr(String hexStr) {
        byte[] plain = HexUtils.hexStringToByteArray(hexStr);

        //NUM VALUE
        byte num = plain[INDEX_NUM];

        // NUM_1 create and write
        plain[1] = (byte) (plain[INDEX_NUM] + ENCRYPT_PLUS_NUMBER);

        // NUM XOR AFTER NUM
        byte[] data = new byte[plain.length - INDEX_DATA_START];
        byte[] newDataByteArr = new byte[plain.length - INDEX_DATA_START];
        System.arraycopy(plain, INDEX_DATA_START, data, 0, data.length);
        for (int i = 0; i < data.length; i++) {
            newDataByteArr[i] = (byte) (plain[i + INDEX_DATA_START] ^ num);
        }

        // xor data write
        System.arraycopy(newDataByteArr, 0, plain, INDEX_DATA_START, newDataByteArr.length);

        // calculate crc16
        int crc16 = CRC16.calcCrc16(plain);

        // return result
        return HexUtils.byteArrayToHexString(plain) + HexUtils.intToHexString(crc16);
    }


}
