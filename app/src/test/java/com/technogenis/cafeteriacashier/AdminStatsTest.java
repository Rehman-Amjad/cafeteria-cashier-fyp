package com.technogenis.cafeteriacashier;

import static org.junit.Assert.assertEquals;

import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.model.ItemSalesModel;
import com.technogenis.cafeteriacashier.util.AdminStats;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AdminStatsTest {

    // HistoryModel(id, balance, payment, rfid, itemName, itemPrice, itemQty)
    private List<HistoryModel> sample() {
        return Arrays.asList(
                new HistoryModel("100", "500", "Card", "R1", "Coke", "60", "2"),
                new HistoryModel("200", "440", "Cash", "R1", "Burger", "50", "1"),
                new HistoryModel("150", "300", "Card", "R2", "Coke", "30", "1")
        );
    }

    @Test
    public void totalRevenue_sumsItemPrice() {
        assertEquals(140, AdminStats.totalRevenue(sample())); // 60+50+30
    }

    @Test
    public void transactionCount_isSize() {
        assertEquals(3, AdminStats.transactionCount(sample()));
    }

    @Test
    public void cardCashSplit_countsByPayment() {
        int[] split = AdminStats.cardCashSplit(sample());
        assertEquals(2, split[0]); // card
        assertEquals(1, split[1]); // cash
    }

    @Test
    public void salesOnDay_sumsWithinRange() {
        // include Ids in [100, 160): the "100" (60) and "150" (30) rows
        assertEquals(90, AdminStats.salesOnDay(sample(), 100L, 160L));
    }

    @Test
    public void latestBalanceByRfid_picksMaxId() {
        Map<String, Integer> bal = AdminStats.latestBalanceByRfid(sample());
        assertEquals(Integer.valueOf(440), bal.get("R1")); // Id 200 wins over 100
        assertEquals(Integer.valueOf(300), bal.get("R2"));
    }

    @Test
    public void itemSales_groupsByNameSummingQtyAndRevenue() {
        List<ItemSalesModel> sales = AdminStats.itemSales(sample());
        assertEquals(2, sales.size());
        ItemSalesModel coke = sales.get(0).getName().equals("Coke") ? sales.get(0) : sales.get(1);
        assertEquals(3, coke.getQtySold());   // 2 + 1
        assertEquals(90, coke.getRevenue());  // 60 + 30
    }
}
