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
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    private String consumerToken() {
        return jwtUtils.generateToken(12, "user_alice", "CONSUMER", null);
    }

    @Test
    void checkout_withItems_success() throws Exception {
        // Alice (userId=12) has cart items: product_id=1 (qty=1), product_id=2 (qty=2)
        mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + consumerToken())
                        .contentType("application/json")
                        .content("{\"userId\":12,\"storeId\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("下单成功")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PICKUP-")));
    }

    @Test
    void checkout_emptyCart_fails() throws Exception {
        // Charlie (userId=14) has 1 cart item, clear it first
        String token = jwtUtils.generateToken(14, "user_charlie", "CONSUMER", null);
        mockMvc.perform(delete("/api/cart/user/14")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"userId\":14,\"storeId\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("购物车为空")));
    }

    @Test
    void pickup_validCode_success() throws Exception {
        // Seed data has order with pickup_code 'PICKUP-711-X9Y8Z7' in AWAITING_PICKUP status
        String token = jwtUtils.generateToken(2, "emp_bj_01", "EMPLOYEE", 1);

        mockMvc.perform(post("/api/orders/pickup")
                        .header("Authorization", "Bearer " + token)
                        .param("pickupCode", "PICKUP-711-X9Y8Z7"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("核销成功")));
    }

    @Test
    void pickup_invalidCode_fails() throws Exception {
        String token = jwtUtils.generateToken(2, "emp_bj_01", "EMPLOYEE", 1);

        mockMvc.perform(post("/api/orders/pickup")
                        .header("Authorization", "Bearer " + token)
                        .param("pickupCode", "INVALID-CODE"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("无效")));
    }

    @Test
    void getUserOrders_returnsList() throws Exception {
        // Bob (userId=13) has 2 orders in seed data
        String token = jwtUtils.generateToken(13, "user_bob", "CONSUMER", null);

        mockMvc.perform(get("/api/orders/user/13")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getOrderDetails_returnsVO() throws Exception {
        // Order 1 has 1 order item in seed data
        String token = jwtUtils.generateToken(13, "user_bob", "CONSUMER", null);

        mockMvc.perform(get("/api/orders/1/details")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1));
    }
}
