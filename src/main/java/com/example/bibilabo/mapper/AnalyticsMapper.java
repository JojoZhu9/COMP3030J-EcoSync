package com.example.bibilabo.mapper;

import com.example.bibilabo.entity.*;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface AnalyticsMapper {

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED')")
    BigDecimal getTotalSales();

    @Select("SELECT COUNT(*) FROM orders WHERE status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED')")
    Integer getTotalOrderCount();

    @Select("SELECT COUNT(*) FROM users")
    Integer getTotalUsers();

    @Select("SELECT COUNT(*) FROM standard_products")
    Integer getTotalProducts();

    @Select("SELECT COUNT(*) FROM stores")
    Integer getTotalStores();

    // Total savings = sum(quantity * (normal_price - actual_price))
    @Select("SELECT COALESCE(SUM(oi.quantity * (sp.normal_price - oi.actual_price)), 0) " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "JOIN expiring_products ep ON oi.product_id = ep.product_id " +
            "JOIN standard_products sp ON ep.barcode = sp.barcode " +
            "WHERE o.status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED')")
    BigDecimal getTotalSavings();

    // Waste reduction monetary value = sum of normal prices of sold near-expiry items
    @Select("SELECT COALESCE(SUM(oi.quantity * sp.normal_price), 0) " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "JOIN expiring_products ep ON oi.product_id = ep.product_id " +
            "JOIN standard_products sp ON ep.barcode = sp.barcode " +
            "WHERE o.status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED')")
    BigDecimal getWasteReductionValue();

    // Waste reduction in kg: estimate 0.5kg per unit as a reasonable food waste metric
    @Select("SELECT COALESCE(SUM(oi.quantity), 0) * 0.5 " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "WHERE o.status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED')")
    BigDecimal getWasteReductionKgRaw();

    // Sales trend by day (last 30 days)
    @Select("SELECT DATE(o.created_at) as period, COALESCE(SUM(o.total_amount), 0) as amount, COUNT(*) as orderCount " +
            "FROM orders o " +
            "WHERE o.status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED') " +
            "AND o.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE(o.created_at) " +
            "ORDER BY period")
    @Results({
        @Result(property = "period", column = "period"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "orderCount", column = "orderCount")
    })
    List<SalesTrend> getDailySalesTrend();

    // Sales trend by week (last 12 weeks)
    @Select("SELECT CONCAT(YEAR(o.created_at), '-W', WEEK(o.created_at)) as period, " +
            "COALESCE(SUM(o.total_amount), 0) as amount, COUNT(*) as orderCount " +
            "FROM orders o " +
            "WHERE o.status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED') " +
            "AND o.created_at >= DATE_SUB(CURDATE(), INTERVAL 12 WEEK) " +
            "GROUP BY YEAR(o.created_at), WEEK(o.created_at) " +
            "ORDER BY YEAR(o.created_at), WEEK(o.created_at)")
    List<SalesTrend> getWeeklySalesTrend();

    // Sales trend by month (last 12 months)
    @Select("SELECT DATE_FORMAT(o.created_at, '%Y-%m') as period, " +
            "COALESCE(SUM(o.total_amount), 0) as amount, COUNT(*) as orderCount " +
            "FROM orders o " +
            "WHERE o.status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED') " +
            "AND o.created_at >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "GROUP BY DATE_FORMAT(o.created_at, '%Y-%m') " +
            "ORDER BY period")
    List<SalesTrend> getMonthlySalesTrend();

    // Top 10 selling products
    @Select("SELECT sp.product_name as productName, sp.product_name_en as productNameEn, SUM(oi.quantity) as totalSold, SUM(oi.quantity * oi.actual_price) as revenue " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "JOIN expiring_products ep ON oi.product_id = ep.product_id " +
            "JOIN standard_products sp ON ep.barcode = sp.barcode " +
            "WHERE o.status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED') " +
            "GROUP BY sp.barcode, sp.product_name, sp.product_name_en " +
            "ORDER BY totalSold DESC " +
            "LIMIT 10")
    List<TopProduct> getTopProducts();

    // Store sales comparison
    @Select("SELECT s.store_name as storeName, s.store_name_en as storeNameEn, COALESCE(SUM(o.total_amount), 0) as amount, COUNT(*) as orderCount " +
            "FROM orders o " +
            "JOIN stores s ON o.store_id = s.store_id " +
            "WHERE o.status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED') " +
            "GROUP BY s.store_id, s.store_name, s.store_name_en " +
            "ORDER BY amount DESC")
    List<StoreSales> getStoreSalesComparison();

    // Order status distribution
    @Select("SELECT status, COUNT(*) as count FROM orders WHERE status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED', 'CANCELLED') GROUP BY status")
    List<Map<String, Object>> getOrderStatusDistribution();

    // User growth by day (last 30 days)
    @Select("SELECT DATE(created_at) as period, COUNT(*) as count FROM users " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY period")
    List<Map<String, Object>> getDailyUserGrowth();

    // User growth by month (last 12 months)
    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m') as period, COUNT(*) as count FROM users " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m') " +
            "ORDER BY period")
    List<Map<String, Object>> getMonthlyUserGrowth();

    // Discount rate distribution for sold items
    @Select("SELECT CASE " +
            "WHEN oi.actual_price / sp.normal_price >= 0.9 THEN '0-10% Off' " +
            "WHEN oi.actual_price / sp.normal_price >= 0.8 THEN '10-20% Off' " +
            "WHEN oi.actual_price / sp.normal_price >= 0.7 THEN '20-30% Off' " +
            "WHEN oi.actual_price / sp.normal_price >= 0.6 THEN '30-40% Off' " +
            "WHEN oi.actual_price / sp.normal_price >= 0.5 THEN '40-50% Off' " +
            "WHEN oi.actual_price / sp.normal_price >= 0.4 THEN '50-60% Off' " +
            "WHEN oi.actual_price / sp.normal_price >= 0.3 THEN '60-70% Off' " +
            "WHEN oi.actual_price / sp.normal_price >= 0.2 THEN '70-80% Off' " +
            "WHEN oi.actual_price / sp.normal_price >= 0.1 THEN '80-90% Off' " +
            "ELSE '90%+ Off' END as `range`, " +
            "COUNT(*) as count " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "JOIN expiring_products ep ON oi.product_id = ep.product_id " +
            "JOIN standard_products sp ON ep.barcode = sp.barcode " +
            "WHERE o.status IN ('PAID', 'AWAITING_PICKUP', 'COMPLETED') " +
            "GROUP BY `range` " +
            "ORDER BY MIN(oi.actual_price / sp.normal_price) DESC")
    List<DiscountDistribution> getDiscountDistribution();
}
