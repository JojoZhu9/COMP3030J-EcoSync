package com.example.bibilabo.entity;

import java.math.BigDecimal;

public class OrderItem {
    private Integer itemId;
    private Integer orderId;
    private Integer productId;
    private Integer quantity;
    private BigDecimal actualPrice; // 修改：购买时的折后单价

    // --- Getter & Setter ---
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getActualPrice() { return actualPrice; }
    public void setActualPrice(BigDecimal actualPrice) { this.actualPrice = actualPrice; }
}