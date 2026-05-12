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
class ExpiringProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    private String employeeToken() {
        return jwtUtils.generateToken(2, "emp_bj_01", "EMPLOYEE");
    }

    @Test
    void getAllProducts_returnsList() throws Exception {
        mockMvc.perform(get("/api/expiring-products")
                        .header("Authorization", "Bearer " + employeeToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    void getById_existingProduct_returnsProduct() throws Exception {
        mockMvc.perform(get("/api/expiring-products/1")
                        .header("Authorization", "Bearer " + employeeToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.barcode").value("6901234560001"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void getAvailableByStore_store1_returns2Products() throws Exception {
        // Store 1 has products 1 and 2, both AVAILABLE
        mockMvc.perform(get("/api/expiring-products/store/1")
                        .header("Authorization", "Bearer " + employeeToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAvailableByStore_store3_returnsEmpty() throws Exception {
        // Store 3 has product 4 which is SOLD_OUT
        mockMvc.perform(get("/api/expiring-products/store/3")
                        .header("Authorization", "Bearer " + employeeToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createProduct_asEmployee_success() throws Exception {
        mockMvc.perform(post("/api/expiring-products")
                        .header("Authorization", "Bearer " + employeeToken())
                        .contentType("application/json")
                        .content("{\"barcode\":\"6901234560005\",\"storeId\":1," +
                                "\"expirationTime\":\"2026-06-30T23:59:00.000+00:00\"," +
                                "\"remainingStock\":10,\"createdBy\":2}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("上架成功")));
    }

    @Test
    void updateProduct_asEmployee_success() throws Exception {
        mockMvc.perform(put("/api/expiring-products/1")
                        .header("Authorization", "Bearer " + employeeToken())
                        .contentType("application/json")
                        .content("{\"barcode\":\"6901234560001\",\"storeId\":1," +
                                "\"expirationTime\":\"2026-04-14T20:00:00.000+00:00\"," +
                                "\"remainingStock\":1,\"status\":\"AVAILABLE\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("更新成功")));
    }

    @Test
    void deleteProduct_asEmployee_success() throws Exception {
        // Delete product 5 (DISCARDED, not referenced by any order or cart)
        mockMvc.perform(delete("/api/expiring-products/5")
                        .header("Authorization", "Bearer " + employeeToken()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("删除成功")));
    }
}
