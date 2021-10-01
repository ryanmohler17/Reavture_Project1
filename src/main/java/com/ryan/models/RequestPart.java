package com.ryan.models;

import java.util.ArrayList;
import java.util.List;

public class RequestPart {

    private int id;
    private double amount;
    private String description;
    private RequestType type;
    private List<String> images = new ArrayList<>();
    private double rate;

    public RequestPart(double amount, String description, RequestType type) {
        this.amount = amount;
        this.description = description;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
