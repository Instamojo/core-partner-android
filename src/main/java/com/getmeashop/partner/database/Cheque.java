package com.getmeashop.partner.database;

/**
 * Created by nikka on 11/8/15.
 */
public class Cheque {
    private String number = "";
    private String image = "";
    private String id = "";
    private String r_uri = "";
    private String bank_name = "";
    private String deposited = "";
    private String plan = "";
    private String amount = "";
    private String months = "";
    private String startDate = "";

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getMonths() {
        return months;
    }

    public void setMonths(String months) {
        this.months = months;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getR_uri() {
        return r_uri;
    }

    public void setR_uri(String r_uri) {
        this.r_uri = r_uri;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getDeposited() {
        return deposited;
    }

    public void setDeposited(String deposited) {
        this.deposited = deposited;
    }

}
