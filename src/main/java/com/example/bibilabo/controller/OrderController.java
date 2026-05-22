package com.example.bibilabo.controller;

import com.example.bibilabo.entity.CheckoutResult;
import com.example.bibilabo.entity.Order;
import com.example.bibilabo.entity.OrderVO;
import com.example.bibilabo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "5. 交易与核销模块", description = "提供用户自提下单、金额计算、库存扣减、及店员扫码核销功能")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/{orderId}/cancel")
    public Map<String, Object> cancelOrder(@PathVariable Integer orderId) {
        Map<String, Object> result = new HashMap<>();
        try {
            String msg = orderService.cancelOrder(orderId);
            result.put("success", true);
            result.put("message", msg);
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/checkout")
    @Operation(summary = "提交自提订单", description = "按店铺自动拆单，扣减库存和账户余额，计算JSON阶梯折扣，生成核销自提码")
    public CheckoutResult checkout(@RequestBody Map<String, Object> payload) {
        Integer userId = (Integer) payload.get("userId");
        Integer storeId = (Integer) payload.get("storeId");

        try {
            return orderService.checkout(userId, storeId);
        } catch (Exception e) {
            throw new RuntimeException("Order placement failed: " + e.getMessage());
        }
    }

    @PostMapping("/pickup")
    @Operation(summary = "店员核销提货码", description = "消费者出示提货码，店员调用此接口完成交易")
    public String confirmPickup(@RequestParam("pickupCode") String pickupCode) {
        return orderService.confirmPickup(pickupCode);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户历史订单")
    public List<Order> getUserOrders(@Parameter(description = "用户ID") @PathVariable("userId") Integer userId) {
        return orderService.getUserOrders(userId);
    }

    @GetMapping("/{orderId}/details")
    @Operation(summary = "获取订单详情", description = "返回订单基本信息及包含的所有商品明细")
    public OrderVO getOrderDetails(@PathVariable("orderId") Integer orderId) {
        return orderService.getOrderDetails(orderId);
    }

    // 修改点：放弃使用 Order 对象接收，直接用 Map 精准捕捉前端传来的 {"status": "xxx"}
    @PutMapping("/{orderId}")
    @Operation(summary = "修改订单状态", description = "店员端修改订单为PENDING/PAID/AWAITING_PICKUP/COMPLETED/CANCELLED")
    public CompletableFuture<String> updateOrderStatus(@PathVariable("orderId") Integer orderId, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");

        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("Order status cannot be empty");
        }

        // 异步更新状态
        return orderService.updateOrderStatus(orderId, status);
    }
}
