package com.example.bibilabo.service;

import com.example.bibilabo.entity.Order;
import com.example.bibilabo.entity.OrderVO;

import java.util.List;

public interface OrderService {
    // 下单现在只需要知道是谁在哪个店提货
    String checkout(Integer userId, Integer storeId);

    List<Order> getUserOrders(Integer userId);

    // 🔥 新增：更新订单状态
    void updateOrderStatus(Integer orderId, String status);

    // 店员扫码核销
    String confirmPickup(String pickupCode);

    OrderVO getOrderDetails(Integer orderId);
    String cancelOrder(Integer orderId);
}