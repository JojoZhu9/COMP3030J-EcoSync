package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.User;
import com.example.bibilabo.mapper.UserMapper;
import com.example.bibilabo.service.UserService;
import com.example.bibilabo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String inputMd5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!user.getPasswordHash().equals(inputMd5Password)) {
            throw new RuntimeException("密码错误");
        }

        if ("BANNED".equals(user.getStatus())) {
            throw new RuntimeException("该账户已被封禁，请联系管理员");
        }

        return jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());
    }

    @Override
    public String createUser(User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        String rawPassword = user.getPasswordHash();
        String md5Password = DigestUtils.md5DigestAsHex(rawPassword.getBytes());
        user.setPasswordHash(md5Password);

        if (user.getRole() == null) {
            user.setRole("CONSUMER");
        }
        if (user.getStatus() == null) {
            user.setStatus("NORMAL");
        }

        userMapper.insert(user);
        return "用户创建成功，用户ID: " + user.getUserId();
    }

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

    // 🔥 核心漏洞修复：安全地更新用户信息（包括新加的 userAddress），并防止密码反复加密
    @Override
    public String updateUser(User user) {
        User existingUser = userMapper.findById(user.getUserId());
        if (existingUser == null) {
            throw new RuntimeException("要更新的用户不存在");
        }

        // 获取前端传来的密码
        String incomingPassword = user.getPasswordHash();

        if (incomingPassword != null && !incomingPassword.trim().isEmpty()) {
            // 【修复关键点】判断传来的密码是否和数据库里存的旧密文一模一样
            if (incomingPassword.equals(existingUser.getPasswordHash())) {
                // 一模一样，说明前端传回了旧的密文，不需要重新加密
                user.setPasswordHash(existingUser.getPasswordHash());
            } else {
                // 不一样，说明前端传了新的明文密码，必须进行 MD5 加密
                String md5Password = DigestUtils.md5DigestAsHex(incomingPassword.getBytes());
                user.setPasswordHash(md5Password);
            }
        } else {
            // 前端没传密码，沿用数据库里的旧密文
            user.setPasswordHash(existingUser.getPasswordHash());
        }

        userMapper.update(user);
        return "用户更新成功";
    }

    @Override
    public String deleteUser(Integer userId) {
        userMapper.deleteById(userId);
        return "用户删除成功";
    }
}