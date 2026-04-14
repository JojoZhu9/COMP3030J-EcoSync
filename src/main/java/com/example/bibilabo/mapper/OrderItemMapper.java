package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.OrderItem;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    @Insert("INSERT INTO order_items(order_id, product_id, quantity, actual_price) " +
            "VALUES(#{orderId}, #{productId}, #{quantity}, #{actualPrice})")
    @Options(useGeneratedKeys = true, keyProperty = "itemId")
    int insert(OrderItem orderItem);

    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    List<OrderItem> findByOrderId(Integer orderId);
}