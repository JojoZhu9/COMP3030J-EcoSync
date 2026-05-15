package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.StandardProduct;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface StandardProductMapper {

    // 🔥 修改：只查询状态为 ACTIVE 或 NULL (兼容老数据) 的商品，从而在页面上隐藏已被删除的商品
    @Select("SELECT * FROM standard_products WHERE status = 'ACTIVE' OR status IS NULL")
    List<StandardProduct> findAll();

    @Select("SELECT * FROM standard_products WHERE barcode = #{barcode}")
    StandardProduct findByBarcode(String barcode);

    // 🔥 修改：插入时带上 status 字段
    @Insert("INSERT INTO standard_products(barcode, product_name, normal_price, discount_rates, image_url, status) " +
            "VALUES(#{barcode}, #{productName}, #{normalPrice}, #{discountRates}, #{imageUrl}, #{status})")
    int insert(StandardProduct product);

    // 🔥 修改：更新时带上 status 字段
    @Update("UPDATE standard_products SET product_name = #{productName}, " +
            "normal_price = #{normalPrice}, discount_rates = #{discountRates}, image_url = #{imageUrl}, status = #{status} " +
            "WHERE barcode = #{barcode}")
    int update(StandardProduct product);

    // 🔥🔥🔥 核心修复：将 DELETE 语句改为 UPDATE 语句，避开外键约束，实现软删除！
    @Update("UPDATE standard_products SET status = 'INACTIVE' WHERE barcode = #{barcode}")
    int deleteByBarcode(String barcode);
}