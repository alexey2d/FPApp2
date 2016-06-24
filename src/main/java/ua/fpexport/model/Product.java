package ua.fpexport.model;

import java.math.BigDecimal;

/**
 * Created by al on 18.06.2016.
 */
public class Product {
    private long id;
    private String name;
    private Measure measure; // кг или шт
    private BigDecimal quantity; //целое или вещественное (3 знака после запятой)

    public Product(long id, String name, Measure measure, BigDecimal quantity) {
        this.id = id;
        this.name = name;
        this.measure = measure;
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Measure getMeasure() {
        return measure;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
