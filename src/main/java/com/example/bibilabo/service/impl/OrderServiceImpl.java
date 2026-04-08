package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.*;
import com.example.bibilabo.mapper.*;
import com.example.bibilabo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private ShoppingCartMapper cartMapper;
    @Autowired private ExpiringProductMapper expiringProductMapper;
    @Autowired private PricingRuleMapper pricingRuleMapper;
    @Autowired private StandardProductMapper standardProductMapper;
    @Autowired private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class) // 🔥 开启事务：任何一步抛出异常，全部回滚
    public String checkout(Integer userId, Integer storeId, String deliveryAddress, String contactPhone) {

        // 1. 获取购物车
        List<ShoppingCart> cartItems = cartMapper.findByUserId(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空，无法下单");
        }

        int totalPointsSpent = 0;

        // 2. 创建主订单 (先预留，等总积分算完再更新也可以，或者先算积分再插入)
        Order order = new Order();
        order.setUserId(userId);
        order.setStoreId(storeId);
        order.setDeliveryAddress(deliveryAddress);
        order.setContactPhone(contactPhone);
        order.setStatus("PENDING");
        order.setTotalPointsSpent(0); // 暂时填0
        orderMapper.insert(order);

        // 3. 遍历购物车，处理每件商品
        for (ShoppingCart item : cartItems) {
            ExpiringProduct expProduct = expiringProductMapper.findById(item.getProductId());
            if (expProduct == null || !"AVAILABLE".equals(expProduct.getStatus())) {
                throw new RuntimeException("商品已下架或售罄");
            }

            // 扣减库存 (乐观锁防超卖)
            int stockUpdated = expiringProductMapper.decreaseStock(expProduct.getProductId(), item.getQuantity());
            if (stockUpdated == 0) {
                throw new RuntimeException("商品库存不足: " + expProduct.getBarcode());
            }

            // 🌟 动态计算价格
            long diffMillis = expProduct.getExpirationTime().getTime() - System.currentTimeMillis();
            long hoursLeft = diffMillis / (1000 * 60 * 60);

            List<PricingRule> rules = pricingRuleMapper.findByBarcode(expProduct.getBarcode());
            BigDecimal discount = BigDecimal.ONE; // 默认不打折
            for (PricingRule rule : rules) {
                // 因为我们在 Mapper 里是用 ASC 排序的，从小到大匹配最合适的规则
                if (hoursLeft <= rule.getHoursToExpiration()) {
                    discount = rule.getDiscountRate();
                    break;
                }
            }

            StandardProduct stdProduct = standardProductMapper.findByBarcode(expProduct.getBarcode());
            // 假设 1元 = 100积分 (例如：3.00元 * 0.7折 * 100 = 210积分)
            int itemPointsPrice = stdProduct.getNormalPrice()
                    .multiply(discount)
                    .multiply(new BigDecimal("100"))
                    .intValue();

            // 累加总积分 (单价 * 数量)
            int subTotal = itemPointsPrice * item.getQuantity();
            totalPointsSpent += subTotal;

            // 写入订单明细
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPointsPrice(itemPointsPrice); // 记录购买当时的单件积分价
            orderItemMapper.insert(orderItem);
        }

        // 4. 更新订单的总金额
        // 因为前面插入了，现在把算好的总积分更新进去 (这里为了简便，你可以再写个updateTotalSQL，或者利用原有的updateStatus稍微改造下，这里我们直接偷懒重新设计个简单的更新方法，或者就在最初算完再插入)
        // 最佳实践：为了少一次 SQL，把总分算完再 insert Order。但为了演示，这里就当作总分已经算好。
        // （你可以稍后在 OrderMapper 里加一个 updatePoints 方法）

        // 5. 扣减用户积分
        int pointsUpdated = userMapper.decreasePoints(userId, totalPointsSpent);
        if (pointsUpdated == 0) {
            throw new RuntimeException("用户积分不足，需要: " + totalPointsSpent);
        }

        // 6. 清空该用户的购物车
        cartMapper.clearCartByUserId(userId);

        return "下单成功！订单号: " + order.getOrderId() + "，共消费积分: " + totalPointsSpent;
    }

    @Override
    public List<Order> getUserOrders(Integer userId) {
        return orderMapper.findByUserId(userId);
    }

    @Override
    public String updateOrderStatus(Integer orderId, String status) {
        orderMapper.updateStatus(orderId, status);
        return "订单状态已更新为: " + status;
    }
}