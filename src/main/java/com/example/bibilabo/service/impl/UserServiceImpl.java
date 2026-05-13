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
    public String createUser(User user) {
        if (user.getPasswordHash() != null) {
            user.setPasswordHash(DigestUtils.md5DigestAsHex(user.getPasswordHash().getBytes()));
        }
        int rows = userMapper.insert(user);
        return rows > 0 ? "注册成功" : "注册失败";
    }

    @Override
    public String updateUser(User user) {
        // 1. 获取数据库中的原始对象
        User existingUser = userMapper.findById(user.getUserId());
        if (existingUser == null) {
            return "用户不存在";
        }

        // 2. 字段合并：仅更新前端传来的非空属性
        if (user.getUsername() != null) existingUser.setUsername(user.getUsername());
        if (user.getPhoneNumber() != null) existingUser.setPhoneNumber(user.getPhoneNumber());
        if (user.getUserAddress() != null) existingUser.setUserAddress(user.getUserAddress());
        if (user.getBalance() != null) existingUser.setBalance(user.getBalance());
        if (user.getRole() != null) existingUser.setRole(user.getRole());
        if (user.getStatus() != null) existingUser.setStatus(user.getStatus());

        // 3. 密码逻辑：判断是否需要重新加密
        String incomingPwd = user.getPasswordHash();
        if (incomingPwd != null && !incomingPwd.trim().isEmpty()) {
            // 如果传入的字符串和数据库存的密文不同，视为新密码，进行 MD5 加密
            if (!incomingPwd.equals(existingUser.getPasswordHash())) {
                String encrypted = DigestUtils.md5DigestAsHex(incomingPwd.getBytes());
                existingUser.setPasswordHash(encrypted);
            }
            // 如果相同，说明前端传回的是旧的密文，不作处理
        }

        // 4. 执行更新
        int rows = userMapper.update(existingUser);
        return rows > 0 ? "用户更新成功" : "更新失败";
    }

    @Override
    public String deleteUser(Integer userId) {
        int rows = userMapper.deleteById(userId);
        return rows > 0 ? "删除成功" : "删除失败";
    }

    // 假设你的 login 方法如下，请确保加密方式一致
    @Override
    public String login(String username, String password) throws Exception {
        User user = userMapper.findByUsername(username);
        if (user == null) throw new Exception("用户不存在");

        // 使用相同的 MD5 算法校验
        String encryptedInput = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedInput.equals(user.getPasswordHash())) {
            throw new Exception("密码错误");
        }

        return "fake-jwt-token-for-" + username; // 此处应为生成 JWT 的逻辑
    }
}