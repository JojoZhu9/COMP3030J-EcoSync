package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.User;
import com.example.bibilabo.mapper.UserMapper;
import com.example.bibilabo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        // TODO: 真正的企业级项目在这里需要对 passwordHash 进行 BCrypt 加密
        userMapper.insert(user);
        return "用户创建成功，用户ID: " + user.getUserId();
    }

    @Override
    public String updateUser(User user) {
        userMapper.update(user);
        return "用户信息更新成功";
    }

    @Override
    public String deleteUser(Integer userId) {
        userMapper.deleteById(userId);
        return "用户删除成功";
    }

    @Override
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // TODO: 企业级项目这里需要用 BCrypt 匹配密码。由于我们数据库里存的是明文或简单的 hash_123，这里先直接比较
        if (!user.getPasswordHash().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        // 密码正确，生成并返回 JWT Token
        return com.example.bibilabo.utils.JwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());
    }
}