package com.technogenis.cafeteriacashier.model;

public class HistoryModel {

    String Id;
    String customerblance;
    String customerpayment;
    String customerrfid;
    String itemname;
    String itemprice;
    String itemqty;

    // Constructor with parameters
    public HistoryModel(String id, String customerBalance, String customerPayment, String customerRfid, String itemName, String itemPrice, String itemqty) {
        this.Id = id;
        this.customerblance = customerBalance;
        this.customerpayment = customerPayment;
        this.customerrfid = customerRfid;
        this.itemname = itemName;
        this.itemprice = itemPrice;
        this.itemqty = itemqty;
    }

    // Default constructor
    public HistoryModel() {
    }

    // Getters and Setters with default values for missing data
    public String getId() {
        return Id != null ? Id : "N/A"; // Return "N/A" if Id is null
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getCustomerBalance() {
        return customerblance != null ? customerblance : "0.00"; // Default to "0.00" if null
    }

    public void setCustomerBalance(String customerBalance) {
        this.customerblance = customerBalance;
    }

    public String getCustomerPayment() {
        return customerpayment != null ? customerpayment : "0.00"; // Default to "0.00" if null
    }

    public void setCustomerPayment(String customerPayment) {
        this.customerpayment = customerPayment;
    }

    public String getCustomerRfid() {
        return customerrfid != null ? customerrfid : "Unknown"; // Default to "Unknown" if null
    }

    public void setCustomerRfid(String customerRfid) {
        this.customerrfid = customerRfid;
    }

    public String getItemName() {
        return itemname != null ? itemname : "Unnamed Item"; // Default to "Unnamed Item" if null
    }

    public void setItemName(String itemName) {
        this.itemname = itemName;
    }

    public String getItemPrice() {
        return itemprice != null ? itemprice : "0.00"; // Default to "0.00" if null
    }

    public void setItemPrice(String itemPrice) {
        this.itemprice = itemPrice;
    }

    public String getItemQty() {
        return itemqty != null ? itemqty : "0"; // Default to "0" if null
    }

    public void setItemQty(String itemQty) {
        this.itemqty = itemQty;
    }
}
