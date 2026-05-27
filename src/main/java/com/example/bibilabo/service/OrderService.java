package com.example.bibilabo.service;

import com.example.bibilabo.entity.CheckoutResult;
import com.example.bibilabo.entity.Order;
import com.example.bibilabo.entity.OrderVO;

import java.util.List;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OrderService {
    // 下单按店铺拆单，返回多个订单信息
    CheckoutResult checkout(Integer userId, Integer storeId, List<Integer> productIds);

    List<Order> getUserOrders(Integer userId);

    List<Order> getOrdersByStoreId(Integer storeId);

    // 新增：更新订单状态（异步）
    CompletableFuture<String> updateOrderStatus(Integer orderId, String status);

    // 店员扫码核销
    String confirmPickup(String pickupCode);

    OrderVO getOrderDetails(Integer orderId);
    String cancelOrder(Integer orderId);
}
