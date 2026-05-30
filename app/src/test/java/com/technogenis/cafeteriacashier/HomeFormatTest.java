package com.technogenis.cafeteriacashier;

import static org.junit.Assert.assertEquals;

import com.technogenis.cafeteriacashier.util.HomeFormat;

import org.junit.Test;

public class HomeFormatTest {

    @Test
    public void groupedAmount_addsThousandsSeparator() {
        assertEquals("0", HomeFormat.groupedAmount(0));
        assertEquals("60", HomeFormat.groupedAmount(60));
        assertEquals("1,250", HomeFormat.groupedAmount(1250));
        assertEquals("4,675", HomeFormat.groupedAmount(4675));
    }

    @Test
    public void greetingIndex_byHourOfDay() {
        assertEquals(0, HomeFormat.greetingIndex(0));   // morning
        assertEquals(0, HomeFormat.greetingIndex(8));   // morning
        assertEquals(1, HomeFormat.greetingIndex(12));  // afternoon
        assertEquals(1, HomeFormat.greetingIndex(16));  // afternoon
        assertEquals(2, HomeFormat.greetingIndex(17));  // evening
        assertEquals(2, HomeFormat.greetingIndex(23));  // evening
    }
}
