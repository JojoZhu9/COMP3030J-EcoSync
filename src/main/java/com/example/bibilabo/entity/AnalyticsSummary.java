package com.example.bibilabo.entity;

import java.math.BigDecimal;

public class AnalyticsSummary {
    private BigDecimal totalSales;
    private BigDecimal totalSavings;
    private BigDecimal wasteReductionValue;
    private Integer wasteReductionKg;
    private Integer totalOrders;
    private Integer totalUsers;
    private Integer totalProducts;
    private Integer totalStores;

    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }

    public BigDecimal getTotalSavings() { return totalSavings; }
    public void setTotalSavings(BigDecimal totalSavings) { this.totalSavings = totalSavings; }

    public BigDecimal getWasteReductionValue() { return wasteReductionValue; }
    public void setWasteReductionValue(BigDecimal wasteReductionValue) { this.wasteReductionValue = wasteReductionValue; }

    public Integer getWasteReductionKg() { return wasteReductionKg; }
    public void setWasteReductionKg(Integer wasteReductionKg) { this.wasteReductionKg = wasteReductionKg; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public Integer getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }

    public Integer getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }

    public Integer getTotalStores() { return totalStores; }
    public void setTotalStores(Integer totalStores) { this.totalStores = totalStores; }
}
