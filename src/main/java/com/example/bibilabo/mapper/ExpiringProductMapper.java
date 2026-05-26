package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.ExpiringProduct;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ExpiringProductMapper {

    @Select("SELECT * FROM expiring_products WHERE expiration_time > NOW()")
    List<ExpiringProduct> findAll();

    @Select("SELECT * FROM expiring_products WHERE product_id = #{productId}")
    ExpiringProduct findById(Integer productId);

    @Select("SELECT * FROM expiring_products WHERE product_id = #{productId} FOR UPDATE")
    ExpiringProduct findByIdForUpdate(@Param("productId") Integer productId);

    // 常用业务查询：根据门店ID查找所有正在售卖的临期商品
    @Select("SELECT * FROM expiring_products WHERE store_id = #{storeId} " +
            "AND status = 'AVAILABLE' AND expiration_time > NOW()")
    List<ExpiringProduct> findAvailableByStore(Integer storeId);

    @Insert("INSERT INTO expiring_products(barcode, store_id, expiration_time, remaining_stock, status, created_by) " +
            "VALUES(#{barcode}, #{storeId}, #{expirationTime}, #{remainingStock}, #{status}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "productId")
    int insert(ExpiringProduct product);

    @Update("UPDATE expiring_products SET barcode = #{barcode}, store_id = #{storeId}, " +
            "expiration_time = #{expirationTime}, remaining_stock = #{remainingStock}, " +
            "status = #{status} WHERE product_id = #{productId}")
    int update(ExpiringProduct product);

    // 🔥 核心逻辑：利用乐观锁思想安全扣减库存（防止超卖），库存归零自动标记售罄
    @Update("UPDATE expiring_products SET remaining_stock = remaining_stock - #{quantity}, " +
            "status = CASE WHEN remaining_stock - #{quantity} <= 0 THEN 'SOLD_OUT' ELSE status END " +
            "WHERE product_id = #{productId} AND remaining_stock >= #{quantity}")
    int decreaseStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);

    // 增加库存（取消订单时回滚），库存恢复时自动改回 AVAILABLE
    @Update("UPDATE expiring_products SET remaining_stock = remaining_stock + #{quantity}, " +
            "status = CASE WHEN remaining_stock + #{quantity} > 0 AND status = 'SOLD_OUT' THEN 'AVAILABLE' ELSE status END " +
            "WHERE product_id = #{productId}")
    int increaseStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);

    @Delete("DELETE FROM expiring_products WHERE product_id = #{productId}")
    int deleteById(Integer productId);

    @Delete("DELETE FROM expiring_products WHERE barcode = #{barcode}")
    int deleteByBarcode(String barcode);

    @Update("UPDATE expiring_products SET status = 'DISCARDED' " +
            "WHERE expiration_time < NOW() AND status = 'AVAILABLE'")
    int markExpiredAsDiscarded();
}