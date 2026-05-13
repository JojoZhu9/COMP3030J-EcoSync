package com.example.bibilabo.service;

import com.example.bibilabo.entity.User;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Integer userId);
    User getUserByUsername(String username);
    String createUser(User user);
    String updateUser(User user);
    String deleteUser(Integer userId);
    String login(String username, String password);
}