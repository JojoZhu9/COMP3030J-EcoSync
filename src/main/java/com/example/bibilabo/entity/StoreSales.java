package com.example.bibilabo.entity;

import java.math.BigDecimal;

public class StoreSales {
    private String storeName;
    private BigDecimal amount;
    private Integer orderCount;

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
}
