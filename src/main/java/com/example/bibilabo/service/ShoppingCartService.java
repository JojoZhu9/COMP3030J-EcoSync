package com.example.bibilabo.service;

import com.example.bibilabo.entity.ShoppingCart;
import java.util.List;

public interface ShoppingCartService {
    List<ShoppingCart> getCartByUserId(Integer userId);
    String addToCart(ShoppingCart cart);
    String updateCartItemQuantity(Integer cartItemId, Integer quantity);
    String removeCartItem(Integer cartItemId);
    String clearCart(Integer userId);
}