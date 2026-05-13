package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.User;
import com.example.bibilabo.mapper.UserMapper;
import com.example.bibilabo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    @Override
    public User getUserById(Integer userId) {
        return userMapper.findById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public void createUser(User user) {
        // 创建用户时进行 MD5 加密
        if (user.getPasswordHash() != null) {
            user.setPasswordHash(DigestUtils.md5DigestAsHex(user.getPasswordHash().getBytes()));
        }
        userMapper.insert(user);
    }

    @Override
    public String updateUser(User user) {
        // 1. 重要：先根据 ID 查出数据库中现有的完整用户信息
        User existingUser = userMapper.findById(user.getUserId());
        if (existingUser == null) {
            return "用户未找到";
        }

        // 2. 将前端传来的非空字段“覆盖”到现有对象上
        // 这样可以保证 role, status, balance 等前端没传的字段不会丢失
        if (user.getUsername() != null) existingUser.setUsername(user.getUsername());
        if (user.getPhoneNumber() != null) existingUser.setPhoneNumber(user.getPhoneNumber());
        if (user.getUserAddress() != null) existingUser.setUserAddress(user.getUserAddress());
        if (user.getBalance() != null) existingUser.setBalance(user.getBalance());
        if (user.getRole() != null) existingUser.setRole(user.getRole());
        if (user.getStatus() != null) existingUser.setStatus(user.getStatus());

        // 3. 密码处理逻辑
        String incomingPwd = user.getPasswordHash();
        if (incomingPwd != null && !incomingPwd.trim().isEmpty()) {
            // 如果前端传来的密码和数据库现有的 MD5 密文不一样
            // 说明前端传的是“明文新密码”，需要加密
            if (!incomingPwd.equals(existingUser.getPasswordHash())) {
                String encrypted = DigestUtils.md5DigestAsHex(incomingPwd.getBytes());
                existingUser.setPasswordHash(encrypted);
                System.out.println("检测到新密码，已执行 MD5 加密");
            }
            // 如果一样，说明前端传回的是旧密文（改资料时），则无需改动，保留原样
        }

        // 4. 执行更新，传入的是合并后的完整对象
        int rows = userMapper.update(existingUser);

        if (rows > 0) {
            return "用户更新成功";
        } else {
            return "更新失败";
        }
    }

    @Override
    public void deleteUser(Integer userId) {
        userMapper.deleteById(userId);
    }
}