package com.example.bibilabo.service;

import com.example.bibilabo.entity.Order;
import java.util.List;

public interface OrderService {
    // 下单现在只需要知道是谁在哪个店提货
    String checkout(Integer userId, Integer storeId);
    List<Order> getUserOrders(Integer userId);
    String updateOrderStatus(Integer orderId, String status);
    // 新增：店员扫码核销
    String confirmPickup(String pickupCode);
}