package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.*;
import com.example.bibilabo.mapper.AnalyticsMapper;
import com.example.bibilabo.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private AnalyticsMapper analyticsMapper;

    @Override
    public AnalyticsSummary getSummary() {
        AnalyticsSummary summary = new AnalyticsSummary();
        summary.setTotalSales(analyticsMapper.getTotalSales());
        summary.setTotalSavings(analyticsMapper.getTotalSavings());
        summary.setWasteReductionValue(analyticsMapper.getWasteReductionValue());

        BigDecimal kgRaw = analyticsMapper.getWasteReductionKgRaw();
        summary.setWasteReductionKg(kgRaw != null ? kgRaw.setScale(0, RoundingMode.HALF_UP).intValue() : 0);

        summary.setTotalOrders(analyticsMapper.getTotalOrderCount());
        summary.setTotalUsers(analyticsMapper.getTotalUsers());
        summary.setTotalProducts(analyticsMapper.getTotalProducts());
        summary.setTotalStores(analyticsMapper.getTotalStores());
        return summary;
    }

    @Override
    public List<SalesTrend> getSalesTrend(String period) {
        if ("week".equalsIgnoreCase(period)) {
            return analyticsMapper.getWeeklySalesTrend();
        } else if ("month".equalsIgnoreCase(period)) {
            return analyticsMapper.getMonthlySalesTrend();
        }
        return analyticsMapper.getDailySalesTrend();
    }

    @Override
    public List<TopProduct> getTopProducts() {
        return analyticsMapper.getTopProducts();
    }

    @Override
    public List<StoreSales> getStoreSalesComparison() {
        return analyticsMapper.getStoreSalesComparison();
    }

    @Override
    public List<Map<String, Object>> getOrderStatusDistribution() {
        return analyticsMapper.getOrderStatusDistribution();
    }

    @Override
    public List<Map<String, Object>> getUserGrowth(String period) {
        if ("month".equalsIgnoreCase(period)) {
            return analyticsMapper.getMonthlyUserGrowth();
        }
        return analyticsMapper.getDailyUserGrowth();
    }

    @Override
    public List<DiscountDistribution> getDiscountDistribution() {
        return analyticsMapper.getDiscountDistribution();
    }
}
