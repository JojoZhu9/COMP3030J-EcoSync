package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.ShoppingCart;
import com.example.bibilabo.mapper.ShoppingCartMapper;
import com.example.bibilabo.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Override
    public List<ShoppingCart> getCartByUserId(Integer userId) {
        return shoppingCartMapper.findByUserId(userId);
    }

    @Override
    public String addToCart(ShoppingCart cart) {
        // 核心逻辑：检查用户购物车里是否已经有这个商品了
        ShoppingCart existingItem = shoppingCartMapper.findByUserIdAndProductId(cart.getUserId(), cart.getProductId());

        if (existingItem != null) {
            // 如果有了，数量相加
            int newQuantity = existingItem.getQuantity() + cart.getQuantity();
            shoppingCartMapper.updateQuantity(existingItem.getCartItemId(), newQuantity);
            return "购物车已有该商品，数量已更新为: " + newQuantity;
        } else {
            // 如果没有，新增记录
            shoppingCartMapper.insert(cart);
            return "加入购物车成功，购物车项ID: " + cart.getCartItemId();
        }
    }

    @Override
    public String updateCartItemQuantity(Integer cartItemId, Integer quantity) {
        if (quantity <= 0) {
            shoppingCartMapper.deleteById(cartItemId);
            return "数量小于等于0，已从购物车移除";
        }
        shoppingCartMapper.updateQuantity(cartItemId, quantity);
        return "购物车商品数量更新成功";
    }

    @Override
    public String removeCartItem(Integer cartItemId) {
        shoppingCartMapper.deleteById(cartItemId);
        return "商品已从购物车移除";
    }

    @Override
    public String clearCart(Integer userId) {
        shoppingCartMapper.clearCartByUserId(userId);
        return "购物车已清空";
    }
}