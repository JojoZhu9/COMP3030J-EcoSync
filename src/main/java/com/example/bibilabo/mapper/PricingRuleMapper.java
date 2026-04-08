package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.PricingRule;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PricingRuleMapper {

    @Select("SELECT * FROM pricing_rules")
    List<PricingRule> findAll();

    @Select("SELECT * FROM pricing_rules WHERE rule_id = #{ruleId}")
    PricingRule findById(Integer ruleId);

    // 核心查询：根据商品条码获取它的所有降价阶梯，并按距离过期时间从小到大排序
    // 这样在计算价格时，方便匹配最优惠的折扣
    @Select("SELECT * FROM pricing_rules WHERE barcode = #{barcode} ORDER BY hours_to_expiration ASC")
    List<PricingRule> findByBarcode(String barcode);

    @Insert("INSERT INTO pricing_rules(barcode, hours_to_expiration, discount_rate) " +
            "VALUES(#{barcode}, #{hoursToExpiration}, #{discountRate})")
    @Options(useGeneratedKeys = true, keyProperty = "ruleId")
    int insert(PricingRule rule);

    @Update("UPDATE pricing_rules SET barcode = #{barcode}, hours_to_expiration = #{hoursToExpiration}, " +
            "discount_rate = #{discountRate} WHERE rule_id = #{ruleId}")
    int update(PricingRule rule);

    @Delete("DELETE FROM pricing_rules WHERE rule_id = #{ruleId}")
    int deleteById(Integer ruleId);
}