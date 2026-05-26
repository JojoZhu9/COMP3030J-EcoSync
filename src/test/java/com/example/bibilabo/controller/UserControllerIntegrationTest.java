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
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    private String adminToken() {
        return jwtUtils.generateToken(1, "admin_super", "ADMIN", null);
    }

    @Test
    void login_validCredentials_success() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .contentType("application/json")
                        .content("{\"username\":\"user_alice\",\"password\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("登录成功")));
    }

    @Test
    void login_wrongPassword_fail() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .contentType("application/json")
                        .content("{\"username\":\"user_alice\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("登录失败")));
    }

    @Test
    void login_nonexistentUser_fail() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .contentType("application/json")
                        .content("{\"username\":\"nobody\",\"password\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("登录失败")));
    }

    @Test
    void register_newUser_success() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType("application/json")
                        .content("{\"username\":\"test_new_user\",\"passwordHash\":\"123456\",\"role\":\"CONSUMER\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("用户创建成功")));
    }

    @Test
    void getUsers_returnsList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(16)));
    }

    @Test
    void getUserById_returnsUser() throws Exception {
        mockMvc.perform(get("/api/users/12")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user_alice"))
                .andExpect(jsonPath("$.role").value("CONSUMER"));
    }
}
