package ua.fpexport.model;

import java.util.ArrayList;

/**
 * Created by al on 18.06.2016.
 */
public class Document {
    private long id;
    private String date;
    private String customerName;
    private Currency currency;
    private String comment;
    private ArrayList<Product> products;

    public Document(long id, String date, String customerName, Currency currency, String comment) {
        this.id = id;
        this.date = date;
        this.customerName = customerName;
        this.currency = currency;
        this.comment = comment;
    }

    public Document(long id, String date, String customerName, Currency currency, String comment, ArrayList<Product> products) {
        this.id = id;
        this.date = date;
        this.customerName = customerName;
        this.currency = currency;
        this.comment = comment;
        this.products = products;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }
}
