package com.app.util;

import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final String HASH_ALGO = "PBKDF2WithHmacSHA256";
    private static final String VERSION_PREFIX = "p1$";
    private static final int SALT_BYTES = 16;
    private static final int DEFAULT_ITERATIONS = 120000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password, salt, DEFAULT_ITERATIONS, KEY_LENGTH_BITS);
        return VERSION_PREFIX + DEFAULT_ITERATIONS + "$" + toHex(salt) + "$" + toHex(derived);
    }

    public static boolean verify(String plain, String hash) {
        if (plain == null || hash == null || hash.isBlank() || !hash.startsWith(VERSION_PREFIX)) {
            return false;
        }
        String[] parts = hash.split("\\$", 4);
        if (parts.length != 4 || parts[1].isBlank() || parts[2].isBlank() || parts[3].isBlank()) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = fromHex(parts[2]);
            byte[] expected = fromHex(parts[3]);
            byte[] actual = derive(plain, salt, iterations, expected.length * 8);
            return constantTimeEquals(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLengthBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(HASH_ALGO);
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int value = b & 0xff;
            if (value < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(value));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        int length = hex.length();
        if ((length & 1) == 1) {
            throw new IllegalArgumentException("Invalid hex length");
        }
        byte[] out = new byte[length / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = Character.digit(hex.charAt(i * 2), 16);
            int lo = Character.digit(hex.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("Invalid hex value");
            }
            out[i] = (byte) ((hi << 4) + lo);
        }
        return out;
    }

    private static boolean constantTimeEquals(byte[] left, byte[] right) {
        if (left.length != right.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < left.length; i++) {
            diff |= left[i] ^ right[i];
        }
        return diff == 0;
    }

}
