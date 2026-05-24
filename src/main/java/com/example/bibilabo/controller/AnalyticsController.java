package com.example.bibilabo.controller;

import com.example.bibilabo.entity.*;
import com.example.bibilabo.service.AnalyticsService;
import com.example.bibilabo.util.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/summary")
    public AnalyticsSummary summary() {
        return analyticsService.getSummary();
    }

    @GetMapping("/sales-trend")
    public List<SalesTrend> salesTrend(@RequestParam(defaultValue = "day") String period) {
        return analyticsService.getSalesTrend(period);
    }

    @GetMapping("/top-products")
    public List<TopProduct> topProducts(@RequestParam(required = false, defaultValue = "zh") String lang) {
        List<TopProduct> list = analyticsService.getTopProducts();
        if ("en".equalsIgnoreCase(lang)) {
            for (TopProduct p : list) {
                if (p.getProductNameEn() != null && !p.getProductNameEn().isEmpty()) {
                    p.setProductName(p.getProductNameEn());
                }
            }
        }
        return list;
    }

    @GetMapping("/store-sales")
    public List<StoreSales> storeSales(@RequestParam(required = false, defaultValue = "zh") String lang) {
        List<StoreSales> list = analyticsService.getStoreSalesComparison();
        if ("en".equalsIgnoreCase(lang)) {
            for (StoreSales s : list) {
                if (s.getStoreNameEn() != null && !s.getStoreNameEn().isEmpty()) {
                    s.setStoreName(s.getStoreNameEn());
                }
            }
        }
        return list;
    }

    @GetMapping("/order-status")
    public List<Map<String, Object>> orderStatus() {
        return analyticsService.getOrderStatusDistribution();
    }

    @GetMapping("/user-growth")
    public List<Map<String, Object>> userGrowth(@RequestParam(defaultValue = "day") String period) {
        return analyticsService.getUserGrowth(period);
    }

    @GetMapping("/discount-distribution")
    public List<DiscountDistribution> discountDistribution() {
        return analyticsService.getDiscountDistribution();
    }
}
