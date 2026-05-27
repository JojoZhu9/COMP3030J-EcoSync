package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    // 获取某个用户购物车里的所有商品
    @Select("SELECT * FROM shopping_cart WHERE user_id = #{userId}")
    List<ShoppingCart> findByUserId(Integer userId);

    // 查询该用户是否已经加购过该批次的商品
    @Select("SELECT * FROM shopping_cart WHERE user_id = #{userId} AND product_id = #{productId}")
    ShoppingCart findByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    @Select("SELECT * FROM shopping_cart WHERE cart_item_id = #{cartItemId}")
    ShoppingCart findById(Integer cartItemId);

    @Insert("INSERT INTO shopping_cart(user_id, product_id, quantity) VALUES(#{userId}, #{productId}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "cartItemId")
    int insert(ShoppingCart cart);

    @Update("UPDATE shopping_cart SET quantity = #{quantity} WHERE cart_item_id = #{cartItemId}")
    int updateQuantity(@Param("cartItemId") Integer cartItemId, @Param("quantity") Integer quantity);

    @Delete("DELETE FROM shopping_cart WHERE cart_item_id = #{cartItemId}")
    int deleteById(Integer cartItemId);

    // 下单成功后，一键清空该用户的购物车
    @Delete("DELETE FROM shopping_cart WHERE user_id = #{userId}")
    int clearCartByUserId(Integer userId);
}