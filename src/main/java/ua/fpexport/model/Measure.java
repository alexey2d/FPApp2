package ua.fpexport.model;

/**
 * Created by al on 18.06.2016.
 */
public enum Measure {
    KILO("кг"),
    PC("шт");

    private String measure;

    Measure(String measure) {
        this.measure = measure;
    }

    public String getValue() {
        return this.measure;
    }
}
