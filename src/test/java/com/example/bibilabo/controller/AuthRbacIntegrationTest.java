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
class AuthRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/cart/user/12"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/cart/user/12")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consumerPostExpiringProducts_returns403() throws Exception {
        String token = jwtUtils.generateToken(12, "user_alice", "CONSUMER");

        mockMvc.perform(post("/api/expiring-products")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"barcode\":\"test\",\"storeId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeePostExpiringProducts_notForbidden() throws Exception {
        String token = jwtUtils.generateToken(2, "emp_bj_01", "EMPLOYEE");

        // Employee 应该通过 RBAC 检查（不返回 403 即通过）
        mockMvc.perform(post("/api/expiring-products")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"barcode\":\"6901234560001\",\"storeId\":1,\"expirationTime\":\"2026-12-31T23:59:00.000+00:00\",\"remainingStock\":5,\"status\":\"AVAILABLE\",\"createdBy\":2}"))
                .andExpect(status().isOk());
    }

    @Test
    void optionsRequest_passes() throws Exception {
        mockMvc.perform(options("/api/cart/user/12"))
                .andExpect(status().isOk());
    }

    @Test
    void loginEndpoint_noToken_ok() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .contentType("application/json")
                        .content("{\"username\":\"user_alice\",\"password\":\"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getUsers_noToken_ok() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    void getProducts_noToken_ok() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }
}
