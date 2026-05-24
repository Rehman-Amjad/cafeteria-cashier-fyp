package com.technogenis.cafeteriacashier.util;

public final class SafeParse {

    private SafeParse() {}

    public static int parseIntOr(String value, int defaultVal) {
        if (value == null) return defaultVal;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return defaultVal;
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            try {
                return (int) Math.round(Double.parseDouble(trimmed));
            } catch (NumberFormatException e2) {
                return defaultVal;
            }
        }
    }

    public static long parseLongOr(String value, long defaultVal) {
        if (value == null) return defaultVal;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return defaultVal;
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
