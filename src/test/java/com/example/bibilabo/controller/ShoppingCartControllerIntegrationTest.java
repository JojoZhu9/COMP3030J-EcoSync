package com.example.bibilabo.controller;

import com.example.bibilabo.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ShoppingCartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    private String consumerToken() {
        return jwtUtils.generateToken(12, "user_alice", "CONSUMER", null);
    }

    @Test
    void getCartByUserId_returnsItems() throws Exception {
        // Alice (userId=12) has 2 cart items in seed data
        mockMvc.perform(get("/api/cart/user/12")
                        .header("Authorization", "Bearer " + consumerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void addToCart_newItem_success() throws Exception {
        // Add product_id=9 (Cheesecake, store 8) to Alice's cart
        mockMvc.perform(post("/api/cart")
                        .header("Authorization", "Bearer " + consumerToken())
                        .contentType("application/json")
                        .content("{\"userId\":12,\"productId\":9,\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("加入购物车成功")));
    }

    @Test
    void addToCart_existingItem_mergesQuantity() throws Exception {
        // Alice already has product_id=1 with qty=1, adding qty=2 should merge to 3
        mockMvc.perform(post("/api/cart")
                        .header("Authorization", "Bearer " + consumerToken())
                        .contentType("application/json")
                        .content("{\"userId\":12,\"productId\":1,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("3")));
    }

    @Test
    void updateQuantity_setsNewValue() throws Exception {
        // Alice's first cart item has cart_item_id=1
        mockMvc.perform(put("/api/cart/1")
                        .header("Authorization", "Bearer " + consumerToken())
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("更新成功")));
    }

    @Test
    void deleteItem_removesFromCart() throws Exception {
        mockMvc.perform(delete("/api/cart/1")
                        .header("Authorization", "Bearer " + consumerToken()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("移除")));
    }

    @Test
    void clearCart_removesAllItems() throws Exception {
        mockMvc.perform(delete("/api/cart/user/12")
                        .header("Authorization", "Bearer " + consumerToken()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("已清空")));
    }
}
