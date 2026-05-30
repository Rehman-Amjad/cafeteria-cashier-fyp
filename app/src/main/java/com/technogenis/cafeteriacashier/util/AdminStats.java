package com.technogenis.cafeteriacashier.util;

import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.model.ItemSalesModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pure aggregation helpers for the admin dashboard. No Android dependencies. */
public final class AdminStats {

    private static final String PAYMENT_CASH = "Cash";

    private AdminStats() {}

    public static int totalRevenue(List<HistoryModel> items) {
        int sum = 0;
        for (HistoryModel h : items) sum += SafeParse.parseIntOr(h.getItemPrice(), 0);
        return sum;
    }

    public static int transactionCount(List<HistoryModel> items) {
        return items.size();
    }

    /** Returns {cardCount, cashCount}; anything not "Cash" counts as Card. */
    public static int[] cardCashSplit(List<HistoryModel> items) {
        int card = 0, cash = 0;
        for (HistoryModel h : items) {
            if (PAYMENT_CASH.equalsIgnoreCase(h.getCustomerPayment())) cash++;
            else card++;
        }
        return new int[]{card, cash};
    }

    /** Sum of itemprice for rows whose Id timestamp is in [startMillis, endMillis). */
    public static int salesOnDay(List<HistoryModel> items, long startMillis, long endMillis) {
        int sum = 0;
        for (HistoryModel h : items) {
            long id = SafeParse.parseLongOr(h.getId(), 0L);
            if (id >= startMillis && id < endMillis) {
                sum += SafeParse.parseIntOr(h.getItemPrice(), 0);
            }
        }
        return sum;
    }

    /** Per RFID, the customerblance of the transaction with the largest Id. */
    public static Map<String, Integer> latestBalanceByRfid(List<HistoryModel> items) {
        Map<String, Long> latestId = new LinkedHashMap<>();
        Map<String, Integer> balance = new LinkedHashMap<>();
        for (HistoryModel h : items) {
            String rfid = h.getCustomerRfid();
            long id = SafeParse.parseLongOr(h.getId(), 0L);
            Long prev = latestId.get(rfid);
            if (prev == null || id > prev) {
                latestId.put(rfid, id);
                balance.put(rfid, SafeParse.parseIntOr(h.getCustomerBalance(), 0));
            }
        }
        return balance;
    }

    /** Group by item name, summing quantity and revenue. Unsorted; caller sorts. */
    public static List<ItemSalesModel> itemSales(List<HistoryModel> items) {
        Map<String, ItemSalesModel> map = new LinkedHashMap<>();
        for (HistoryModel h : items) {
            String name = h.getItemName();
            ItemSalesModel agg = map.get(name);
            if (agg == null) {
                agg = new ItemSalesModel(name, 0, 0);
                map.put(name, agg);
            }
            agg.add(SafeParse.parseIntOr(h.getItemQty(), 0),
                    SafeParse.parseIntOr(h.getItemPrice(), 0));
        }
        return new ArrayList<>(map.values());
    }
}
