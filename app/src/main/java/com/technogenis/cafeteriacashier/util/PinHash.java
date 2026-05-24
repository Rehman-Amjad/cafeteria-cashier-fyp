package com.technogenis.cafeteriacashier.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Opt-in PIN hashing helper.
 *
 * Not wired into the live login path — the existing FYP database stores
 * plaintext PINs. Once you migrate the database to store SHA-256 hashes,
 * switch the comparison in LoginActivity to use {@link #sha256(String)}.
 */
public final class PinHash {

    private PinHash() {}

    public static String sha256(String input) {
        if (input == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
