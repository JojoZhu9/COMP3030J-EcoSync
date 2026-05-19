package com.example.bibilabo.entity;

import java.math.BigDecimal;

public class TopProduct {
    private String productName;
    private Integer totalSold;
    private BigDecimal revenue;

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getTotalSold() { return totalSold; }
    public void setTotalSold(Integer totalSold) { this.totalSold = totalSold; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
}
