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

    @Override
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 使用 MD5 加密前端传来的明文密码
        String inputMd5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 拿加密后的输入密码，和数据库里存的密文进行比对
        if (!user.getPasswordHash().equals(inputMd5Password)) {
            throw new RuntimeException("密码错误");
        }

        // 校验通过，检查账号是否被封禁
        if ("BANNED".equals(user.getStatus())) {
            throw new RuntimeException("该账户已被封禁，请联系管理员");
        }

        // 密码正确且状态正常，生成并返回 JWT Token
        return JwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());
    }

    @Override
    public String createUser(User user) {
        // 校验用户名是否已存在
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 拦截明文密码，进行 MD5 加密入库
        String rawPassword = user.getPasswordHash();
        String md5Password = DigestUtils.md5DigestAsHex(rawPassword.getBytes());
        user.setPasswordHash(md5Password);

        // 设置默认角色和状态
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

    @Override
    public String updateUser(User user) {
        userMapper.update(user);
        return "用户更新成功";
    }

    @Override
    public String deleteUser(Integer userId) {
        userMapper.deleteById(userId);
        return "用户删除成功";
    }
}