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
    @Insert("INSERT INTO standard_products(barcode, product_name, normal_price, discount_rates) " +
            "VALUES(#{barcode}, #{productName}, #{normalPrice}, #{discountRates})")
    int insert(StandardProduct product);

    // 增加了 discount_rates 字段的更新
    @Update("UPDATE standard_products SET product_name = #{productName}, " +
            "normal_price = #{normalPrice}, discount_rates = #{discountRates} " +
            "WHERE barcode = #{barcode}")
    int update(StandardProduct product);

    @Delete("DELETE FROM standard_products WHERE barcode = #{barcode}")
    int deleteByBarcode(String barcode);
}