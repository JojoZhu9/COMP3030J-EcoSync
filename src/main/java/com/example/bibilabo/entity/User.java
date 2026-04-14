package com.example.bibilabo.entity;

import java.math.BigDecimal;
import java.util.Date;

public class User {
    private Integer userId;
    private String username;
    private String passwordHash;
    private String role;
    private String status; // 新增：状态 ENUM('NORMAL', 'BANNED')
    private Integer storeId;
    private BigDecimal balance; // 修改：积分(points) 变为 余额(balance)
    private String phoneNumber;
    private Date createdAt;

    // --- Getter & Setter ---
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}