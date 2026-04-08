package com.example.bibilabo.service;

import com.example.bibilabo.entity.Order;
import java.util.List;

public interface OrderService {
    // 核心下单接口
    String checkout(Integer userId, Integer storeId, String deliveryAddress, String contactPhone);
    List<Order> getUserOrders(Integer userId);
    String updateOrderStatus(Integer orderId, String status);
}