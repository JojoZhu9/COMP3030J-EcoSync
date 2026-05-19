package com.example.bibilabo.service;

import com.example.bibilabo.entity.*;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {
    AnalyticsSummary getSummary();
    List<SalesTrend> getSalesTrend(String period);
    List<TopProduct> getTopProducts();
    List<StoreSales> getStoreSalesComparison();
    List<Map<String, Object>> getOrderStatusDistribution();
    List<Map<String, Object>> getUserGrowth(String period);
    List<DiscountDistribution> getDiscountDistribution();
}
