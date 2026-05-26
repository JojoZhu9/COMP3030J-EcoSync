package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.Order;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderMapper {
    @Insert("INSERT INTO orders(user_id, store_id, total_amount, pickup_code, status, created_at) " +
            "VALUES(#{userId}, #{storeId}, #{totalAmount}, #{pickupCode}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    int insert(Order order);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Order> findByUserId(Integer userId);

    @Select("SELECT * FROM orders WHERE store_id = #{storeId} ORDER BY created_at DESC")
    List<Order> findByStoreId(Integer storeId);

    @Select("SELECT * FROM orders WHERE order_id = #{orderId}")
    Order findById(Integer orderId);

    @Update("UPDATE orders SET status = #{status} WHERE order_id = #{orderId}")
    int updateStatus(@Param("orderId") Integer orderId, @Param("status") String status);

    @Update("UPDATE orders SET total_amount = #{amount} WHERE order_id = #{orderId}")
    int updateTotalAmount(@Param("orderId") Integer orderId, @Param("amount") BigDecimal amount);

    // 🔥 新增：店员通过核销码完成订单
    @Update("UPDATE orders SET status = 'COMPLETED', completed_at = NOW() WHERE pickup_code = #{pickupCode} AND status != 'COMPLETED'")
    int completeOrderByPickupCode(String pickupCode);
}