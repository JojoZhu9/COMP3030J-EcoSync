package com.example.bibilabo.entity;

import java.util.List;

// 继承原有的 Order 类，额外增加一个 items 列表
public class OrderVO extends Order {
    private List<OrderItem> items;

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}