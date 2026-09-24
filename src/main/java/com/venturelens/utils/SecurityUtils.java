package com.venturelens.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Standard Java Security Utility for hashing passwords using SHA-256.
 * Strictly uses java.security.MessageDigest from the core JDK.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Hashes a plain-text password using standard SHA-256 and returns a 64-character hex string.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            plainPassword = "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in standard JDK", e);
        }
    }

    /**
     * Validates whether a plain password matches a stored SHA-256 hex string.
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (storedHash == null || plainPassword == null) {
            return false;
        }
        String computedHash = hashPassword(plainPassword);
        return MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
