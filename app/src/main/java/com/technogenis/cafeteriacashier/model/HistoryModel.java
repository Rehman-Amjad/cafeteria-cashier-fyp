package com.technogenis.cafeteriacashier.model;

public class HistoryModel {

    String Id;
    String customerblance;
    String customerpayment;
    String customerrfid;
    String itemname;
    String itemprice;
    String itemqty;

    public HistoryModel(String id, String customerblance, String customerpayment, String customerrfid, String itemname, String itemprice, String itemqty) {
        Id = id;
        this.customerblance = customerblance;
        this.customerpayment = customerpayment;
        this.customerrfid = customerrfid;
        this.itemname = itemname;
        this.itemprice = itemprice;
        this.itemqty = itemqty;
    }

    public HistoryModel() {
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getCustomerblance() {
        return customerblance;
    }

    public void setCustomerblance(String customerblance) {
        this.customerblance = customerblance;
    }

    public String getCustomerpayment() {
        return customerpayment;
    }

    public void setCustomerpayment(String customerpayment) {
        this.customerpayment = customerpayment;
    }

    public String getCustomerrfid() {
        return customerrfid;
    }

    public void setCustomerrfid(String customerrfid) {
        this.customerrfid = customerrfid;
    }

    public String getItemname() {
        return itemname;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname;
    }

    public String getItemprice() {
        return itemprice;
    }

    public void setItemprice(String itemprice) {
        this.itemprice = itemprice;
    }

    public String getItemqty() {
        return itemqty;
    }

    public void setItemqty(String itemqty) {
        this.itemqty = itemqty;
    }
}
