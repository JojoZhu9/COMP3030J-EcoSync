package com.example.bibilabo.service.impl;

import com.example.bibilabo.constant.OrderStatus;
import com.example.bibilabo.constant.ProductStatus;
import com.example.bibilabo.entity.*;
import com.example.bibilabo.mapper.*;
import com.example.bibilabo.service.OrderService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    public CheckoutResult checkout(Integer userId, Integer ignoredStoreId) {

        List<ShoppingCart> cartItems = cartMapper.findByUserId(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty, cannot place order");
        }

        ObjectMapper mapper = new ObjectMapper();

        // 1. 先遍历一遍：加锁校验库存、按店铺分组、预计算价格
        Map<Integer, List<ShoppingCart>> itemsByStore = new LinkedHashMap<>();
        Map<Integer, BigDecimal> storeTotals = new HashMap<>();
        Map<Integer, ExpiringProduct> lockedProducts = new HashMap<>();
        BigDecimal overallTotal = BigDecimal.ZERO;

        for (ShoppingCart item : cartItems) {
            ExpiringProduct expProduct = expiringProductMapper.findByIdForUpdate(item.getProductId());
            if (expProduct == null || !ProductStatus.AVAILABLE.equals(expProduct.getStatus())) {
                throw new RuntimeException("Product is unavailable or sold out");
            }
            if (expProduct.getRemainingStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + expProduct.getBarcode());
            }
            lockedProducts.put(item.getProductId(), expProduct);

            Integer actualStoreId = expProduct.getStoreId();
            itemsByStore.computeIfAbsent(actualStoreId, k -> new ArrayList<>()).add(item);

            StandardProduct stdProduct = standardProductMapper.findByBarcode(expProduct.getBarcode());
            BigDecimal itemPrice = calculateItemPrice(expProduct, stdProduct, mapper);
            BigDecimal subTotal = itemPrice.multiply(new BigDecimal(item.getQuantity()));
            storeTotals.merge(actualStoreId, subTotal, BigDecimal::add);
            overallTotal = overallTotal.add(subTotal);
        }

        // 2. 预检查余额（加锁）
        User user = userMapper.findByIdForUpdate(userId);
        if (user == null || user.getBalance().compareTo(overallTotal) < 0) {
            throw new RuntimeException("Insufficient balance, need to pay: ¥" + overallTotal);
        }

        // 3. 逐店铺创建订单、扣库存、写明细
        List<OrderResult> createdOrders = new ArrayList<>();

        for (Map.Entry<Integer, List<ShoppingCart>> entry : itemsByStore.entrySet()) {
            Integer storeId = entry.getKey();
            List<ShoppingCart> items = entry.getValue();

            String pickupCode = "PICKUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Order order = new Order();
            order.setUserId(userId);
            order.setStoreId(storeId);
            order.setPickupCode(pickupCode);
            order.setTotalAmount(BigDecimal.ZERO);
            order.setStatus(OrderStatus.PAID);
            orderMapper.insert(order);

            BigDecimal storeTotal = BigDecimal.ZERO;

            for (ShoppingCart item : items) {
                ExpiringProduct expProduct = lockedProducts.get(item.getProductId());

                int stockUpdated = expiringProductMapper.decreaseStock(expProduct.getProductId(), item.getQuantity());
                if (stockUpdated == 0) {
                    throw new RuntimeException("Insufficient stock for product: " + expProduct.getBarcode());
                }

                StandardProduct stdProduct = standardProductMapper.findByBarcode(expProduct.getBarcode());
                BigDecimal itemPrice = calculateItemPrice(expProduct, stdProduct, mapper);
                BigDecimal subTotal = itemPrice.multiply(new BigDecimal(item.getQuantity()));
                storeTotal = storeTotal.add(subTotal);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getOrderId());
                orderItem.setProductId(item.getProductId());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setActualPrice(itemPrice);
                orderItemMapper.insert(orderItem);
            }

            orderMapper.updateTotalAmount(order.getOrderId(), storeTotal);

            OrderResult result = new OrderResult();
            result.setOrderId(order.getOrderId());
            result.setStoreId(storeId);
            result.setPickupCode(pickupCode);
            result.setTotalAmount(storeTotal);
            createdOrders.add(result);
        }

        // 4. 扣款并清空购物车
        int balanceUpdated = userMapper.decreaseBalance(userId, overallTotal);
        if (balanceUpdated == 0) {
            throw new RuntimeException("Insufficient balance, need to pay: ¥" + overallTotal);
        }

        cartMapper.clearCartByUserId(userId);

        CheckoutResult result = new CheckoutResult();
        result.setOrders(createdOrders);
        result.setMessage("Order placed successfully! Total: ¥" + overallTotal);
        return result;
    }

    private BigDecimal calculateItemPrice(ExpiringProduct expProduct, StandardProduct stdProduct, ObjectMapper mapper) {
        BigDecimal discount = BigDecimal.ONE;
        try {
            List<BigDecimal> discountRates = mapper.readValue(stdProduct.getDiscountRates(), new TypeReference<List<BigDecimal>>(){});
            if (discountRates == null || discountRates.isEmpty()) {
                return stdProduct.getNormalPrice();
            }
            long diffMillis = expProduct.getExpirationTime().getTime() - System.currentTimeMillis();
            int hoursLeft = (int) Math.max(0, diffMillis / (1000 * 60 * 60));
            if (hoursLeft < 12) {
                int index = 11 - hoursLeft;
                index = Math.max(0, Math.min(index, discountRates.size() - 1));
                discount = discountRates.get(index);
            }
        } catch (Exception e) {
            System.err.println("Discount parse failed, fallback to full price: " + e.getMessage());
        }
        return stdProduct.getNormalPrice().multiply(discount);
    }

    @Override
    public List<Order> getUserOrders(Integer userId) {
        return orderMapper.findByUserId(userId);
    }

    @Override
    public List<Order> getOrdersByStoreId(Integer storeId) {
        return orderMapper.findByStoreId(storeId);
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<String> updateOrderStatus(Integer orderId, String status) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("Order does not exist");
        }
        // 已完成的订单不允许修改状态
        if (OrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new RuntimeException("Completed orders cannot be modified");
        }
        int rows = orderMapper.updateStatus(orderId, status);
        if (rows == 0) {
            throw new RuntimeException("Order does not exist or update failed");
        }
        return CompletableFuture.completedFuture("Status updated successfully");
    }

    @Override
    public String confirmPickup(String pickupCode) {
        int updated = orderMapper.completeOrderByPickupCode(pickupCode);
        if (updated > 0) {
            return "Pickup verified successfully!";
        }
        return "Invalid pickup code or order already verified";
    }

    @Override
    public OrderVO getOrderDetails(Integer orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("Order does not exist");
        }

        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);

        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setUserId(order.getUserId());
        vo.setStoreId(order.getStoreId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPickupCode(order.getPickupCode());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setCompletedAt(order.getCompletedAt());
        vo.setItems(items);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String cancelOrder(Integer orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("Order does not exist");
        }

        if (!OrderStatus.PAID.equals(order.getStatus()) && !OrderStatus.AWAITING_PICKUP.equals(order.getStatus())) {
            throw new RuntimeException("Only paid or awaiting pickup orders can be cancelled");
        }

        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        for (OrderItem item : items) {
            expiringProductMapper.increaseStock(item.getProductId(), item.getQuantity());
        }

        userMapper.increaseBalance(order.getUserId(), order.getTotalAmount());

        int rows = orderMapper.updateStatus(orderId, OrderStatus.CANCELLED);
        if (rows == 0) {
            throw new RuntimeException("Failed to cancel order");
        }

        return "Order cancelled, amount refunded to balance";
    }
}
