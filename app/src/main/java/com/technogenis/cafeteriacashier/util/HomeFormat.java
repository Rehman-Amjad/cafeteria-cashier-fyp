package com.technogenis.cafeteriacashier.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Pure presentation helpers for the Home dashboard. No Android dependencies. */
public final class HomeFormat {

    private HomeFormat() {}

    /** Formats an amount with thousands separators, e.g. 4675 -> "4,675". */
    public static String groupedAmount(int amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    /**
     * Maps a 24-hour hour-of-day to a greeting bucket:
     * 0 = morning (&lt;12), 1 = afternoon (&lt;17), 2 = evening.
     */
    public static int greetingIndex(int hourOfDay) {
        if (hourOfDay < 12) return 0;
        if (hourOfDay < 17) return 1;
        return 2;
    }
}
