package com.example.bibilabo.constant;

/**
 * 订单状态常量
 */
public interface OrderStatus {
    String PENDING = "PENDING";                 // 待支付
    String PAID = "PAID";                       // 已支付
    String AWAITING_PICKUP = "AWAITING_PICKUP"; // 待自提/已支付等待提货
    String COMPLETED = "COMPLETED";             // 已完成/已核销
    String CANCELLED = "CANCELLED";             // 已取消
}