package com.technogenis.cafeteriacashier.model;

public class HistoryModel {

    String Id;
    String customerblance;
    String customerpayment;
    String customerrfid;
    String itemname;
    String itemprice;
    String itemqty;

    public HistoryModel() {
    }

    public HistoryModel(String id, String customerBalance, String customerPayment,
                        String customerRfid, String itemName, String itemPrice, String itemqty) {
        this.Id = id;
        this.customerblance = customerBalance;
        this.customerpayment = customerPayment;
        this.customerrfid = customerRfid;
        this.itemname = itemName;
        this.itemprice = itemPrice;
        this.itemqty = itemqty;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getCustomerBalance() {
        return customerblance != null ? customerblance : "0";
    }

    public void setCustomerBalance(String customerBalance) {
        this.customerblance = customerBalance;
    }

    public String getCustomerPayment() {
        return customerpayment != null ? customerpayment : "";
    }

    public void setCustomerPayment(String customerPayment) {
        this.customerpayment = customerPayment;
    }

    public String getCustomerRfid() {
        return customerrfid != null ? customerrfid : "";
    }

    public void setCustomerRfid(String customerRfid) {
        this.customerrfid = customerRfid;
    }

    public String getItemName() {
        return itemname != null ? itemname : "Unnamed Item";
    }

    public void setItemName(String itemName) {
        this.itemname = itemName;
    }

    public String getItemPrice() {
        return itemprice != null ? itemprice : "0";
    }

    public void setItemPrice(String itemPrice) {
        this.itemprice = itemPrice;
    }

    public String getItemQty() {
        return itemqty != null ? itemqty : "0";
    }

    public void setItemQty(String itemQty) {
        this.itemqty = itemQty;
    }
}
