package com.example.demo.utils;

import java.security.MessageDigest;

public class SHA1 {
    public static String encode(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(str.getBytes());
            byte[] result = md.digest();
            StringBuilder hexStr = new StringBuilder();
            for (byte b : result) {
                int val = ((int) b) & 0xff;
                if (val < 16) hexStr.append("0");
                hexStr.append(Integer.toHexString(val));
            }
            return hexStr.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
