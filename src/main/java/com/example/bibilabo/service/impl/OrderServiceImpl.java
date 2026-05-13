package com.example.bibilabo.service.impl;

import com.example.bibilabo.constant.OrderStatus; // 引入订单常量
import com.example.bibilabo.constant.ProductStatus; // 引入商品常量
import com.example.bibilabo.entity.*;
import com.example.bibilabo.mapper.*;
import com.example.bibilabo.service.OrderService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private ShoppingCartMapper cartMapper;
    @Autowired private ExpiringProductMapper expiringProductMapper;
    @Autowired private StandardProductMapper standardProductMapper;
    @Autowired private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String checkout(Integer userId, Integer storeId) {

        List<ShoppingCart> cartItems = cartMapper.findByUserId(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空，无法下单");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 1. 生成唯一核销码，并预创建订单
        String pickupCode = "PICKUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order();
        order.setUserId(userId);
        order.setStoreId(storeId);
        order.setPickupCode(pickupCode);
        order.setTotalAmount(BigDecimal.ZERO);
        // 使用常量接口替换硬编码
        order.setStatus(OrderStatus.AWAITING_PICKUP);
        orderMapper.insert(order);

        ObjectMapper mapper = new ObjectMapper();

        // 2. 遍历购物车，处理每件商品
        for (ShoppingCart item : cartItems) {
            ExpiringProduct expProduct = expiringProductMapper.findById(item.getProductId());
            // 使用常量接口替换硬编码
            if (expProduct == null || !ProductStatus.AVAILABLE.equals(expProduct.getStatus())) {
                throw new RuntimeException("商品已下架或售罄");
            }

            int stockUpdated = expiringProductMapper.decreaseStock(expProduct.getProductId(), item.getQuantity());
            if (stockUpdated == 0) {
                throw new RuntimeException("商品库存不足: " + expProduct.getBarcode());
            }

            StandardProduct stdProduct = standardProductMapper.findByBarcode(expProduct.getBarcode());

            // 🌟 解析 JSON 折扣数组逻辑
            BigDecimal discount = BigDecimal.ONE;
            try {
                // 将字符串 "[1.0, 0.9, ...]" 转换为 List<BigDecimal>
                List<BigDecimal> discountRates = mapper.readValue(stdProduct.getDiscountRates(), new TypeReference<List<BigDecimal>>(){});

                long diffMillis = expProduct.getExpirationTime().getTime() - System.currentTimeMillis();
                int hoursLeft = (int) Math.max(0, diffMillis / (1000 * 60 * 60));

                // 只有距离过期小于12小时才打折
                if (hoursLeft < 12) {
                    // 数组第0位是倒数第12小时，第11位是最后1小时
                    int index = 11 - hoursLeft;
                    index = Math.max(0, Math.min(index, discountRates.size() - 1));
                    discount = discountRates.get(index);
                }
            } catch (Exception e) {
                System.err.println("折扣解析失败，按原价结算: " + e.getMessage());
            }

            BigDecimal itemPrice = stdProduct.getNormalPrice().multiply(discount);
            BigDecimal subTotal = itemPrice.multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(subTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setActualPrice(itemPrice);
            orderItemMapper.insert(orderItem);
        }

        // 3. 更新总金额并扣款
        orderMapper.updateTotalAmount(order.getOrderId(), totalAmount);

        int balanceUpdated = userMapper.decreaseBalance(userId, totalAmount);
        if (balanceUpdated == 0) {
            throw new RuntimeException("余额不足，需要支付: ¥" + totalAmount);
        }

        // 4. 清空购物车
        cartMapper.clearCartByUserId(userId);

        return "下单成功！自提码: " + pickupCode + "，共消费: ¥" + totalAmount;
    }

    @Override
    public List<Order> getUserOrders(Integer userId) {
        return orderMapper.findByUserId(userId);
    }

    @Override
    public String updateOrderStatus(Integer orderId, String status) {
        orderMapper.updateStatus(orderId, status);
        return "订单状态已更新";
    }

    @Override
    public String confirmPickup(String pickupCode) {
        int updated = orderMapper.completeOrderByPickupCode(pickupCode);
        if (updated > 0) {
            return "自提核销成功！";
        }
        return "无效的自提码或该订单已核销";
    }

    @Override
    public OrderVO getOrderDetails(Integer orderId) {
        // 1. 查询订单主表信息
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 查询该订单关联的所有明细信息
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);

        // 3. 组装成 OrderVO
        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setUserId(order.getUserId());
        vo.setStoreId(order.getStoreId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPickupCode(order.getPickupCode());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setCompletedAt(order.getCompletedAt());

        vo.setItems(items); // 塞入商品明细

        return vo;
    }
}