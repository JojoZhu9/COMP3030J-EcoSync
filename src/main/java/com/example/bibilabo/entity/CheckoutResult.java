package com.example.bibilabo.entity;

import java.util.List;

public class CheckoutResult {
    private List<OrderResult> orders;
    private String message;

    public List<OrderResult> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderResult> orders) {
        this.orders = orders;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
