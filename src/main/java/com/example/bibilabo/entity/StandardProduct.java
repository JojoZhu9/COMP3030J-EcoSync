package com.example.bibilabo.entity;

import java.math.BigDecimal;

public class StandardProduct {
    private String barcode;
    private String productName;
    private BigDecimal normalPrice;
    private String discountRates;
    private String imageUrl;

    // 🔥 新增：商品状态字段，用于实现软删除
    private String status;

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getNormalPrice() { return normalPrice; }
    public void setNormalPrice(BigDecimal normalPrice) { this.normalPrice = normalPrice; }

    public String getDiscountRates() { return discountRates; }
    public void setDiscountRates(String discountRates) { this.discountRates = discountRates; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // 🔥 新增：Getter 和 Setter
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}