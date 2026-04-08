package com.example.bibilabo.service;

import com.example.bibilabo.entity.ExpiringProduct;
import java.util.List;

public interface ExpiringProductService {
    List<ExpiringProduct> getAllProducts();
    ExpiringProduct getProductById(Integer productId);
    List<ExpiringProduct> getAvailableProductsByStore(Integer storeId);
    String createProduct(ExpiringProduct product);
    String updateProduct(ExpiringProduct product);
    String deleteProduct(Integer productId);
    // 扣减库存接口
    boolean decreaseStock(Integer productId, Integer quantity);
}