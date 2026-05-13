package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.User;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserMapper {

    // 核心：建立数据库字段与 Java 实体类属性的对应关系
    @Results(id = "userMap", value = {
            @Result(property = "userId", column = "user_id", id = true),
            @Result(property = "passwordHash", column = "password_hash"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "userAddress", column = "user_address"),
            @Result(property = "storeId", column = "store_id"),
            @Result(property = "createdAt", column = "created_at")
    })
    @Select("SELECT * FROM users WHERE user_id = #{userId}")
    User findById(Integer userId);

    @ResultMap("userMap")
    @Select("SELECT * FROM users")
    List<User> findAll();

    @ResultMap("userMap")
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO users(username, password_hash, role, status, store_id, balance, phone_number, user_address) " +
            "VALUES(#{username}, #{passwordHash}, #{role}, #{status}, #{storeId}, #{balance}, #{phoneNumber}, #{userAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);

    @Update("UPDATE users SET password_hash = #{passwordHash}, role = #{role}, status = #{status}, " +
            "store_id = #{storeId}, balance = #{balance}, phone_number = #{phoneNumber}, user_address = #{userAddress} " +
            "WHERE user_id = #{userId}")
    int update(User user);

    @Update("UPDATE users SET balance = balance - #{amount} WHERE user_id = #{userId} AND balance >= #{amount}")
    int decreaseBalance(@Param("userId") Integer userId, @Param("amount") BigDecimal amount);

    @Delete("DELETE FROM users WHERE user_id = #{userId}")
    int deleteById(Integer userId);
}