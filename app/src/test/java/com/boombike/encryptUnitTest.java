package com.boombike;

import com.boombike.omni.OmniUtils;
import com.boombike.util.HexUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Ravy on 2017. 12. 17..
 */

public class encryptUnitTest {

    @Test
    public void testStringToByteArray() throws Exception {
        String str = "FE88";
        byte[] result = HexUtils.hexStringToByteArray(str);
        String actual = HexUtils.byteArrayToHexString(result);
        Assert.assertTrue(str.equalsIgnoreCase(actual));
    }

    @Test
    public void testEncrypt() throws Exception {
        String plainTxt = "FE8811111111882100";
        String expectTxt = "FEBA9999999900A988E991";
        String actualTxt = OmniUtils.toEncryptStr(plainTxt);
        Assert.assertEquals(expectTxt, actualTxt);
    }

    @Test
    public void testDecrypt() throws Exception {
        String cipherTxt = "FEBA9999999908A989883A2B";
        String expectTxt = "FE881111111180210100";
        String actualTxt = OmniUtils.toDecryptStr(cipherTxt);
        Assert.assertEquals(expectTxt, actualTxt);
    }
}
