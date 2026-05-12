package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.User;
import com.example.bibilabo.mapper.UserMapper;
import com.example.bibilabo.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void login_validCredentials_returnsToken() {
        User user = new User();
        user.setUserId(12);
        user.setUsername("user_alice");
        user.setPasswordHash(DigestUtils.md5DigestAsHex("1".getBytes()));
        user.setRole("CONSUMER");
        user.setStatus("NORMAL");

        when(userMapper.findByUsername("user_alice")).thenReturn(user);
        when(jwtUtils.generateToken(12, "user_alice", "CONSUMER")).thenReturn("test-jwt-token");

        String result = userService.login("user_alice", "1");
        assertEquals("test-jwt-token", result);
        verify(jwtUtils).generateToken(12, "user_alice", "CONSUMER");
    }

    @Test
    void login_nonexistentUser_throws() {
        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login("nonexistent", "1"));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void login_wrongPassword_throws() {
        User user = new User();
        user.setPasswordHash(DigestUtils.md5DigestAsHex("correct_pw".getBytes()));
        user.setStatus("NORMAL");

        when(userMapper.findByUsername("user_alice")).thenReturn(user);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login("user_alice", "wrong_pw"));
        assertEquals("密码错误", ex.getMessage());
    }

    @Test
    void login_bannedUser_throws() {
        User user = new User();
        user.setPasswordHash(DigestUtils.md5DigestAsHex("1".getBytes()));
        user.setStatus("BANNED");

        when(userMapper.findByUsername("user_eve")).thenReturn(user);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login("user_eve", "1"));
        assertTrue(ex.getMessage().contains("封禁"));
    }

    @Test
    void createUser_duplicateUsername_throws() {
        User existing = new User();
        existing.setUsername("user_alice");
        when(userMapper.findByUsername("user_alice")).thenReturn(existing);

        User newUser = new User();
        newUser.setUsername("user_alice");
        newUser.setPasswordHash("123");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.createUser(newUser));
        assertEquals("用户名已存在", ex.getMessage());
    }

    @Test
    void createUser_newUser_hashesPasswordAndSetsDefaults() {
        when(userMapper.findByUsername("new_user")).thenReturn(null);
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId(100);
            return null;
        }).when(userMapper).insert(any(User.class));

        User newUser = new User();
        newUser.setUsername("new_user");
        newUser.setPasswordHash("raw_password");

        String result = userService.createUser(newUser);
        assertTrue(result.contains("用户创建成功"));

        verify(userMapper).insert(argThat(u ->
                u.getPasswordHash().equals(DigestUtils.md5DigestAsHex("raw_password".getBytes()))
                        && "CONSUMER".equals(u.getRole())
                        && "NORMAL".equals(u.getStatus())
        ));
    }

    @Test
    void updateUser_noPassword_keepsOldHash() {
        User existing = new User();
        existing.setUserId(1);
        existing.setPasswordHash("old_hash");
        existing.setRole("CONSUMER");
        existing.setStatus("NORMAL");

        when(userMapper.findById(1)).thenReturn(existing);

        User updateReq = new User();
        updateReq.setUserId(1);
        updateReq.setPasswordHash(null);
        updateReq.setRole("CONSUMER");
        updateReq.setStatus("NORMAL");

        userService.updateUser(updateReq);

        verify(userMapper).update(argThat(u ->
                "old_hash".equals(u.getPasswordHash())
        ));
    }
}
