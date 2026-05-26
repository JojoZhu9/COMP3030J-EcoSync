package com.example.bibilabo.service;

import com.example.bibilabo.entity.ExpiringProduct;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ExpiringProductService {
    List<ExpiringProduct> getAllProducts();
    ExpiringProduct getProductById(Integer productId);
    List<ExpiringProduct> getAvailableProductsByStore(Integer storeId);
    String createProduct(ExpiringProduct product);
    // 更新临期商品（异步）
    CompletableFuture<String> updateProduct(ExpiringProduct product);
    String deleteProduct(Integer productId);
    // 扣减库存接口
    boolean decreaseStock(Integer productId, Integer quantity);
    // 批量标记过期商品为 DISCARDED
    int markExpiredProducts();
}
