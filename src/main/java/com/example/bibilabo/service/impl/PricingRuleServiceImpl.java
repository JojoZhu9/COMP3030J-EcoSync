package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.PricingRule;
import com.example.bibilabo.mapper.PricingRuleMapper;
import com.example.bibilabo.service.PricingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PricingRuleServiceImpl implements PricingRuleService {

    @Autowired
    private PricingRuleMapper pricingRuleMapper;

    @Override
    public List<PricingRule> getAllRules() {
        return pricingRuleMapper.findAll();
    }

    @Override
    public PricingRule getRuleById(Integer ruleId) {
        return pricingRuleMapper.findById(ruleId);
    }

    @Override
    public List<PricingRule> getRulesByBarcode(String barcode) {
        return pricingRuleMapper.findByBarcode(barcode);
    }

    @Override
    public String createRule(PricingRule rule) {
        pricingRuleMapper.insert(rule);
        return "降价规则创建成功，规则ID: " + rule.getRuleId();
    }

    @Override
    public String updateRule(PricingRule rule) {
        pricingRuleMapper.update(rule);
        return "降价规则更新成功";
    }

    @Override
    public String deleteRule(Integer ruleId) {
        pricingRuleMapper.deleteById(ruleId);
        return "降价规则删除成功";
    }
}