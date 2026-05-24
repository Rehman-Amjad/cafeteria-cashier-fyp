package com.technogenis.cafeteriacashier.util;

public final class FirebasePathSanitizer {

    private FirebasePathSanitizer() {}

    public static boolean isValid(String key) {
        if (key == null) return false;
        String trimmed = key.trim();
        if (trimmed.isEmpty()) return false;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '.' || c == '#' || c == '$' || c == '[' || c == ']' || c == '/') {
                return false;
            }
            if (c < 0x20 || c == 0x7F) {
                return false;
            }
        }
        return true;
    }
}
