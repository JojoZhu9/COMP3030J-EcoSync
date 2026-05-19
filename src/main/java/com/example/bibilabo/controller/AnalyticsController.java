package com.example.bibilabo.controller;

import com.example.bibilabo.entity.*;
import com.example.bibilabo.service.AnalyticsService;
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
    public List<TopProduct> topProducts() {
        return analyticsService.getTopProducts();
    }

    @GetMapping("/store-sales")
    public List<StoreSales> storeSales() {
        return analyticsService.getStoreSalesComparison();
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
