package com.example.bibilabo.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Order {
    private Integer orderId;
    private Integer userId;
    private Integer storeId;
    private BigDecimal totalAmount; // 修改：总金额
    private String pickupCode;      // 新增：唯一核销码
    private String status;          // 状态：PENDING, CANCELLED, PAID, AWAITING_PICKUP, COMPLETED
    private Date createdAt;
    private Date completedAt;       // 修改：送达时间变为核销完成时间

    // --- Getter & Setter ---
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPickupCode() { return pickupCode; }
    public void setPickupCode(String pickupCode) { this.pickupCode = pickupCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }
}