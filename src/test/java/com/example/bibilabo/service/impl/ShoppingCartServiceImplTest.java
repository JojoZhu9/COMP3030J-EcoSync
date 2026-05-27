package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.ExpiringProduct;
import com.example.bibilabo.entity.ShoppingCart;
import com.example.bibilabo.mapper.ExpiringProductMapper;
import com.example.bibilabo.mapper.ShoppingCartMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private ExpiringProductMapper expiringProductMapper;

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    private ExpiringProduct createProduct(int stock) {
        ExpiringProduct p = new ExpiringProduct();
        p.setProductId(1);
        p.setRemainingStock(stock);
        return p;
    }

    @Test
    void addToCart_newItem_inserts() {
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(12);
        cart.setProductId(1);
        cart.setQuantity(2);

        when(shoppingCartMapper.findByUserIdAndProductId(12, 1)).thenReturn(null);
        when(expiringProductMapper.findById(1)).thenReturn(createProduct(5));

        shoppingCartService.addToCart(cart);

        verify(shoppingCartMapper).insert(cart);
        verify(shoppingCartMapper, never()).updateQuantity(anyInt(), anyInt());
    }

    @Test
    void addToCart_existingItem_mergesQuantity() {
        ShoppingCart existing = new ShoppingCart();
        existing.setCartItemId(100);
        existing.setUserId(12);
        existing.setProductId(1);
        existing.setQuantity(2);

        ShoppingCart newItem = new ShoppingCart();
        newItem.setUserId(12);
        newItem.setProductId(1);
        newItem.setQuantity(3);

        when(shoppingCartMapper.findByUserIdAndProductId(12, 1)).thenReturn(existing);
        when(expiringProductMapper.findById(1)).thenReturn(createProduct(10));

        String result = shoppingCartService.addToCart(newItem);

        verify(shoppingCartMapper).updateQuantity(100, 5);
        verify(shoppingCartMapper, never()).insert(any());
        assertTrue(result.contains("5"));
    }

    @Test
    void updateQuantity_zero_deletesItem() {
        String result = shoppingCartService.updateCartItemQuantity(1, 0);

        verify(shoppingCartMapper).deleteById(1);
        verify(shoppingCartMapper, never()).updateQuantity(anyInt(), anyInt());
        assertTrue(result.contains("removed"));
    }

    @Test
    void updateQuantity_negative_deletesItem() {
        String result = shoppingCartService.updateCartItemQuantity(1, -5);

        verify(shoppingCartMapper).deleteById(1);
        assertTrue(result.contains("removed"));
    }

    @Test
    void updateQuantity_positive_updates() {
        shoppingCartService.updateCartItemQuantity(1, 3);

        verify(shoppingCartMapper).updateQuantity(1, 3);
        verify(shoppingCartMapper, never()).deleteById(anyInt());
    }

    @Test
    void getCartByUserId_delegates() {
        ShoppingCart item = new ShoppingCart();
        when(shoppingCartMapper.findByUserId(12)).thenReturn(List.of(item));

        List<ShoppingCart> result = shoppingCartService.getCartByUserId(12);

        assertEquals(1, result.size());
        verify(shoppingCartMapper).findByUserId(12);
    }
}
