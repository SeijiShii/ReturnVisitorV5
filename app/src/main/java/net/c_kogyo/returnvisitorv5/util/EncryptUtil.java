package net.c_kogyo.returnvisitorv5.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by SeijiShii on 2017/05/25.
 */

public class EncryptUtil {

    public static String toEncryptedHashValue(String algorithmName, String value) {
        MessageDigest md = null;
        StringBuilder sb = null;
        try {
            md = MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(value.getBytes());
        sb = new StringBuilder();
        for (byte b : md.digest()) {
            String hex = String.format("%02x", b);
            sb.append(hex);
        }
        return sb.toString();
    }
}
