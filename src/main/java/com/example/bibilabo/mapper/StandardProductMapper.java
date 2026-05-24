package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.StandardProduct;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface StandardProductMapper {

    @Select("SELECT * FROM standard_products")
    List<StandardProduct> findAll();

    @Select("SELECT * FROM standard_products WHERE barcode = #{barcode}")
    StandardProduct findByBarcode(String barcode);

    // 增加了 discount_rates 字段的插入
    @Insert("INSERT INTO standard_products(barcode, product_name, product_name_en, normal_price, discount_rates, image_url) " +
            "VALUES(#{barcode}, #{productName}, #{productNameEn}, #{normalPrice}, #{discountRates}, #{imageUrl})")
    int insert(StandardProduct product);

    // 增加了 discount_rates 字段的更新
    @Update("UPDATE standard_products SET product_name = #{productName}, product_name_en = #{productNameEn}, " +
            "normal_price = #{normalPrice}, discount_rates = #{discountRates}, image_url = #{imageUrl} " +
            "WHERE barcode = #{barcode}")
    int update(StandardProduct product);

    @Delete("DELETE FROM standard_products WHERE barcode = #{barcode}")
    int deleteByBarcode(String barcode);
}
