package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.User;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users")
    List<User> findAll();

    @Select("SELECT * FROM users WHERE user_id = #{userId}")
    User findById(Integer userId);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    // 🔥 修改：加入 user_address
    @Insert("INSERT INTO users(username, password_hash, role, status, store_id, balance, phone_number, user_address) " +
            "VALUES(#{username}, #{passwordHash}, #{role}, #{status}, #{storeId}, #{balance}, #{phoneNumber}, #{userAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);

    // 🔥 修改：加入 user_address
    @Update("UPDATE users SET password_hash = #{passwordHash}, role = #{role}, status = #{status}, " +
            "store_id = #{storeId}, balance = #{balance}, phone_number = #{phoneNumber}, user_address = #{userAddress} " +
            "WHERE user_id = #{userId}")
    int update(User user);

    @Update("UPDATE users SET balance = balance - #{amount} WHERE user_id = #{userId} AND balance >= #{amount}")
    int decreaseBalance(@Param("userId") Integer userId, @Param("amount") BigDecimal amount);

    // 增加余额（退款）
    @Update("UPDATE users SET balance = balance + #{amount} WHERE user_id = #{userId}")
    int increaseBalance(@Param("userId") Integer userId, @Param("amount") BigDecimal amount);

    @Delete("DELETE FROM users WHERE user_id = #{userId}")
    int deleteById(Integer userId);
}