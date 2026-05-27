package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.ExpiringProduct;
import com.example.bibilabo.entity.ShoppingCart;
import com.example.bibilabo.mapper.ExpiringProductMapper;
import com.example.bibilabo.mapper.ShoppingCartMapper;
import com.example.bibilabo.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private ExpiringProductMapper expiringProductMapper;

    @Override
    public List<ShoppingCart> getCartByUserId(Integer userId) {
        return shoppingCartMapper.findByUserId(userId);
    }

    @Override
    public String addToCart(ShoppingCart cart) {
        // 核心逻辑：检查用户购物车里是否已经有这个商品了
        ShoppingCart existingItem = shoppingCartMapper.findByUserIdAndProductId(cart.getUserId(), cart.getProductId());

        // Validate stock
        ExpiringProduct expProduct = expiringProductMapper.findById(cart.getProductId());
        if (expProduct == null) {
            throw new RuntimeException("Product not found");
        }
        int availableStock = expProduct.getRemainingStock();

        if (existingItem != null) {
            // 如果有了，数量相加，但不允许超过库存
            int newQuantity = existingItem.getQuantity() + cart.getQuantity();
            if (newQuantity > availableStock) {
                newQuantity = availableStock;
            }
            shoppingCartMapper.updateQuantity(existingItem.getCartItemId(), newQuantity);
            return "Item already in cart, quantity updated to: " + newQuantity;
        } else {
            // 如果没有，新增记录，但数量不能超过库存
            if (cart.getQuantity() > availableStock) {
                cart.setQuantity(availableStock);
            }
            if (cart.getQuantity() <= 0) {
                throw new RuntimeException("Cannot add item with zero stock");
            }
            shoppingCartMapper.insert(cart);
            return "Added to cart successfully, cart item ID: " + cart.getCartItemId();
        }
    }

    @Override
    public String updateCartItemQuantity(Integer cartItemId, Integer quantity) {
        if (quantity <= 0) {
            shoppingCartMapper.deleteById(cartItemId);
            return "Quantity is 0 or less, item removed from cart";
        }
        // Validate against available stock before updating
        ShoppingCart cartItem = shoppingCartMapper.findById(cartItemId);
        if (cartItem != null) {
            ExpiringProduct expProduct = expiringProductMapper.findById(cartItem.getProductId());
            if (expProduct != null && quantity > expProduct.getRemainingStock()) {
                quantity = expProduct.getRemainingStock();
            }
        }
        shoppingCartMapper.updateQuantity(cartItemId, quantity);
        return "Cart item quantity updated successfully";
    }

    @Override
    public String removeCartItem(Integer cartItemId) {
        shoppingCartMapper.deleteById(cartItemId);
        return "Item removed from cart";
    }

    @Override
    public String clearCart(Integer userId) {
        shoppingCartMapper.clearCartByUserId(userId);
        return "Cart cleared successfully";
    }
}