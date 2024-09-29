package com.technogenis.cafeteriacashier.model;

public class CustomerModel {

    // Declare all the required fields
    private String customerPhone;
    private String customerPin;
    private String customerblance;
    private String customernic;
    private String customerrfid;
    private String customername;

    // Constructor with parameters
    public CustomerModel(
            String customerBalance,
                         String customerRfid, String customername,
            String customerPhone,String customerPin,String customernic
    ) {
        this.customerblance = customerBalance;
        this.customerrfid = customerRfid;
        this.customername = customername;
        this.customerPhone = customerPhone;
        this.customerPin = customerPin;
        this.customernic = customernic;
    }

    // Default constructor
    public CustomerModel() {
    }


    public String getCustomerPhone() {
        return customerPhone != null ? customerPhone : "N/A";
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerPin() {
        return customerPin != null ? customerPin : "N/A";
    }

    public void setCustomerPin(String customerPin) {
        this.customerPin = customerPin;
    }

    public String getCustomerBalance() {
        return customerblance != null ? customerblance : "0.00"; // Default to "0.00" if null
    }

    public void setCustomerBalance(String customerBalance) {
        this.customerblance = customerBalance;
    }

    public String getCustomerName() {
        return customername != null ? customername : "Unnamed Customer";
    }

    public void setCustomerName(String customerName) {
        this.customername = customerName;
    }

    public String getCustomerNic() {
        return customernic != null ? customernic : "N/A";
    }

    public void setCustomerNic(String customerNic) {
        this.customernic = customerNic;
    }

    public String getCustomerRfid() {
        return customerrfid != null ? customerrfid : "Unknown"; // Default to "Unknown" if null
    }

    public void setCustomerRfid(String customerRfid) {
        this.customerrfid = customerRfid;
    }

}
