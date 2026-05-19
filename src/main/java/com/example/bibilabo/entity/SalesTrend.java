package com.example.bibilabo.entity;

import java.math.BigDecimal;

public class SalesTrend {
    private String period;
    private BigDecimal amount;
    private Integer orderCount;

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
}
