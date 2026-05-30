package com.technogenis.cafeteriacashier.model;

/** Aggregated sales for one item name (used by the admin Reports screen). */
public class ItemSalesModel {

    private final String name;
    private int qtySold;
    private int revenue;

    public ItemSalesModel(String name, int qtySold, int revenue) {
        this.name = name;
        this.qtySold = qtySold;
        this.revenue = revenue;
    }

    public void add(int qty, int rev) {
        this.qtySold += qty;
        this.revenue += rev;
    }

    public String getName() { return name; }
    public int getQtySold() { return qtySold; }
    public int getRevenue() { return revenue; }
}
