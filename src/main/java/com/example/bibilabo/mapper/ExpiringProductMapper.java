package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.ExpiringProduct;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ExpiringProductMapper {

    @Select("SELECT * FROM expiring_products")
    List<ExpiringProduct> findAll();

    @Select("SELECT * FROM expiring_products WHERE product_id = #{productId}")
    ExpiringProduct findById(Integer productId);

    // 常用业务查询：根据门店ID查找所有正在售卖的临期商品
    @Select("SELECT * FROM expiring_products WHERE store_id = #{storeId} AND status = 'AVAILABLE'")
    List<ExpiringProduct> findAvailableByStore(Integer storeId);

    @Insert("INSERT INTO expiring_products(barcode, store_id, expiration_time, remaining_stock, status, created_by) " +
            "VALUES(#{barcode}, #{storeId}, #{expirationTime}, #{remainingStock}, #{status}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "productId")
    int insert(ExpiringProduct product);

    @Update("UPDATE expiring_products SET barcode = #{barcode}, store_id = #{storeId}, " +
            "expiration_time = #{expirationTime}, remaining_stock = #{remainingStock}, " +
            "status = #{status} WHERE product_id = #{productId}")
    int update(ExpiringProduct product);

    // 🔥 核心逻辑：利用乐观锁思想安全扣减库存（防止超卖）
    @Update("UPDATE expiring_products SET remaining_stock = remaining_stock - #{quantity} " +
            "WHERE product_id = #{productId} AND remaining_stock >= #{quantity}")
    int decreaseStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);

    @Delete("DELETE FROM expiring_products WHERE product_id = #{productId}")
    int deleteById(Integer productId);
}