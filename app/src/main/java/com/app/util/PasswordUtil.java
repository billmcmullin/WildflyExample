package com.app.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) {
                sb.append(String.format("%02x", x & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(String plain, String hash) {
        return hashPassword(plain).equals(hash);
    }

}
