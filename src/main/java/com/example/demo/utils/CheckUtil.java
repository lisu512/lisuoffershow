package com.example.demo.utils;

import java.security.MessageDigest;
import java.util.Arrays;

public class CheckUtil {
    private static final String token = "13123";

    public static boolean checkSignature(String signature, String timestamp, String nonce) {
        String[] strArray = new String[]{token, timestamp, nonce};
        Arrays.sort(strArray);
        StringBuilder sb = new StringBuilder();
        for (String str : strArray) {
            sb.append(str);
        }
        return SHA1.encode(sb.toString()).equals(signature);
    }
}

