package com.example.bibilabo.controller;

import com.example.bibilabo.entity.User;
import com.example.bibilabo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "6. 用户与认证模块", description = "管理消费者、员工和管理员体系，以及登录鉴权")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录 (获取Token)", description = "提交账户密码，验证成功后返回 JWT Token 供后续接口鉴权使用")
    public Map<String, Object> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");
        String token = userService.login(username, password);
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("message", "Login successful");
        return map;
    }

    @GetMapping
    @Operation(summary = "获取所有用户", description = "后台管理接口：列出系统内所有账户")
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取单个用户", description = "根据用户ID查询详情（积分余额、角色等）")
    public User getById(@Parameter(description = "用户ID") @PathVariable("id") Integer id) {
        return userService.getUserById(id);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名查询", description = "系统内部校验用户名是否重复")
    public User getByUsername(@Parameter(description = "用户名") @PathVariable("username") String username) {
        return userService.getUserByUsername(username);
    }

    @PostMapping
    @Operation(summary = "注册新用户", description = "注册新账号并分配角色")
    public String add(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "修改密码、更新默认地址及联系方式")
    public String update(@Parameter(description = "用户ID") @PathVariable("id") Integer id, @RequestBody User user) {
        user.setUserId(id);
        return userService.updateUser(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "注销账号")
    public String delete(@Parameter(description = "用户ID") @PathVariable("id") Integer id) {
        return userService.deleteUser(id);
    }
}