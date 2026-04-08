package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users")
    List<User> findAll();

    @Select("SELECT * FROM users WHERE user_id = #{userId}")
    User findById(Integer userId);

    // 后续登录鉴权必备：根据用户名查找用户
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO users(username, password_hash, role, store_id, points, phone_number, default_address) " +
            "VALUES(#{username}, #{passwordHash}, #{role}, #{storeId}, #{points}, #{phoneNumber}, #{defaultAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);

    @Update("UPDATE users SET password_hash = #{passwordHash}, role = #{role}, store_id = #{storeId}, " +
            "points = #{points}, phone_number = #{phoneNumber}, default_address = #{defaultAddress} " +
            "WHERE user_id = #{userId}")
    int update(User user);

    @Delete("DELETE FROM users WHERE user_id = #{userId}")
    int deleteById(Integer userId);

    @Update("UPDATE users SET points = points - #{points} WHERE user_id = #{userId} AND points >= #{points}")
    int decreasePoints(@Param("userId") Integer userId, @Param("points") Integer points);
}