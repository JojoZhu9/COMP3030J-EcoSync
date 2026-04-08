package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.Order;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface OrderMapper {
    @Insert("INSERT INTO orders(user_id, store_id, total_points_spent, delivery_address, contact_phone, status, created_at) " +
            "VALUES(#{userId}, #{storeId}, #{totalPointsSpent}, #{deliveryAddress}, #{contactPhone}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    int insert(Order order);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Order> findByUserId(Integer userId);

    @Select("SELECT * FROM orders WHERE order_id = #{orderId}")
    Order findById(Integer orderId);

    @Update("UPDATE orders SET status = #{status} WHERE order_id = #{orderId}")
    int updateStatus(@Param("orderId") Integer orderId, @Param("status") String status);
}