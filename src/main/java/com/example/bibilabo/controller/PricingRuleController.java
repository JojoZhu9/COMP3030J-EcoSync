package com.example.bibilabo.controller;

import com.example.bibilabo.entity.PricingRule;
import com.example.bibilabo.service.PricingRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing-rules")
@Tag(name = "2. 降价规则模块", description = "配置不同商品随距离过期时间的阶梯折扣率")
public class PricingRuleController {

    @Autowired
    private PricingRuleService pricingRuleService;

    @GetMapping
    @Operation(summary = "获取所有降价规则", description = "查询系统内配置的所有阶梯打折规则")
    public List<PricingRule> getAll() {
        return pricingRuleService.getAllRules();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询规则", description = "获取某一条具体打折规则的详细信息")
    public PricingRule getById(@Parameter(description = "规则ID") @PathVariable("id") Integer id) {
        return pricingRuleService.getRuleById(id);
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "查询指定商品的规则", description = "根据商品条码获取该商品适用的所有降价阶梯")
    public List<PricingRule> getByBarcode(@Parameter(description = "商品条码") @PathVariable("barcode") String barcode) {
        return pricingRuleService.getRulesByBarcode(barcode);
    }

    @PostMapping
    @Operation(summary = "添加降价规则", description = "为特定商品设置新的时间阶梯和折扣率（如：距过期4小时打5折）")
    public String add(@RequestBody PricingRule rule) {
        return pricingRuleService.createRule(rule);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新降价规则", description = "修改已有打折规则的时间或折扣率")
    public String update(@Parameter(description = "规则ID") @PathVariable("id") Integer id, @RequestBody PricingRule rule) {
        rule.setRuleId(id);
        return pricingRuleService.updateRule(rule);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除降价规则", description = "移除不再使用的折扣阶梯")
    public String delete(@Parameter(description = "规则ID") @PathVariable("id") Integer id) {
        return pricingRuleService.deleteRule(id);
    }
}