package com.example.bibilabo.service;

import com.example.bibilabo.entity.PricingRule;
import java.util.List;

public interface PricingRuleService {
    List<PricingRule> getAllRules();
    PricingRule getRuleById(Integer ruleId);
    List<PricingRule> getRulesByBarcode(String barcode);
    String createRule(PricingRule rule);
    String updateRule(PricingRule rule);
    String deleteRule(Integer ruleId);
}