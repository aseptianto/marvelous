package com.andrioseptianto.marvelous.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    private static final Logger logger = LoggerFactory.getLogger(HashUtil.class);

    // generate hash based on timestamp, private key, and public key for Marvel API authentication
    public static String generateHash(String ts, String privateKey, String publicKey) {
        try {
            String value = ts + privateKey + publicKey;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error generating hash", e);
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}