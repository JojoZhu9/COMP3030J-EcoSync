package com.example.bibilabo.entity;

import java.math.BigDecimal;

public class StandardProduct {
    private String barcode;
    private String productName;
    private String productNameEn;
    private BigDecimal normalPrice;
    private String discountRates; // 映射 MySQL 中的 JSON 数组字符串，例如 "[1.0, 0.9, ...]"
    private String imageUrl;

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductNameEn() { return productNameEn; }
    public void setProductNameEn(String productNameEn) { this.productNameEn = productNameEn; }

    public BigDecimal getNormalPrice() { return normalPrice; }
    public void setNormalPrice(BigDecimal normalPrice) { this.normalPrice = normalPrice; }

    public String getDiscountRates() { return discountRates; }
    public void setDiscountRates(String discountRates) { this.discountRates = discountRates; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
