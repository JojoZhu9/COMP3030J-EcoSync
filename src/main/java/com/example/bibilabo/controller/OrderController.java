package com.example.bibilabo.controller;

import com.example.bibilabo.entity.Order;
import com.example.bibilabo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "5. 交易与订单模块", description = "核心结算逻辑：包含防超卖扣库存、积分计算、订单生成流程")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "提交订单 (结算购物车)", description = "根据购物车内容，动态计算随时间变化的最终折扣，扣减库存、扣除积分并生成订单。")
    public String checkout(@RequestBody Map<String, Object> payload) {
        Integer userId = (Integer) payload.get("userId");
        Integer storeId = (Integer) payload.get("storeId");
        String deliveryAddress = (String) payload.get("deliveryAddress");
        String contactPhone = (String) payload.get("contactPhone");

        try {
            return orderService.checkout(userId, storeId, deliveryAddress, contactPhone);
        } catch (Exception e) {
            return "下单失败: " + e.getMessage();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户订单", description = "获取指定消费者的所有历史订单列表")
    public List<Order> getUserOrders(@Parameter(description = "用户ID") @PathVariable("userId") Integer userId) {
        return orderService.getUserOrders(userId);
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "更新订单状态", description = "管理员或店员更新物流状态（例如：将 PENDING 改为 DELIVERING 或 COMPLETED）")
    public String updateStatus(
            @Parameter(description = "订单ID") @PathVariable("orderId") Integer orderId,
            @Parameter(description = "新状态") @RequestParam("status") String status) {
        return orderService.updateOrderStatus(orderId, status);
    }
}