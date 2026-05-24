package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.Store;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface StoreMapper {

    @Select("SELECT * FROM stores")
    List<Store> findAll();

    @Select("SELECT * FROM stores WHERE store_id = #{storeId}")
    Store findById(Integer storeId);

    // 插入并返回自增主键
    @Insert("INSERT INTO stores(store_name, store_name_en, city, city_en, address, address_en, latitude, longitude) VALUES(#{storeName}, #{storeNameEn}, #{city}, #{cityEn}, #{address}, #{addressEn}, #{latitude}, #{longitude})")
    @Options(useGeneratedKeys = true, keyProperty = "storeId")
    int insert(Store store);

    @Update("UPDATE stores SET store_name = #{storeName}, store_name_en = #{storeNameEn}, city = #{city}, city_en = #{cityEn}, address = #{address}, address_en = #{addressEn}, latitude = #{latitude}, longitude = #{longitude} WHERE store_id = #{storeId}")
    int update(Store store);

    @Delete("DELETE FROM stores WHERE store_id = #{storeId}")
    int deleteById(Integer storeId);
}
