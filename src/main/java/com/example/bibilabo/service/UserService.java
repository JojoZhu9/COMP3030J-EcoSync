package com.example.bibilabo.service;

import com.example.bibilabo.entity.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Integer userId);
    User getUserByUsername(String username);
    String createUser(User user);
    // 更新用户信息（异步）
    CompletableFuture<String> updateUser(User user);
    String deleteUser(Integer userId);
    String login(String username, String password);
}
