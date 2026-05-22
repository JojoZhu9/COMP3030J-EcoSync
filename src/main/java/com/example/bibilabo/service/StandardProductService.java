package com.example.bibilabo.service;

import com.example.bibilabo.entity.StandardProduct;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StandardProductService {
    List<StandardProduct> getAllProducts();
    StandardProduct getProductByBarcode(String barcode);
    void createProduct(StandardProduct product);
    // 更新标准商品（异步）
    CompletableFuture<String> updateProduct(StandardProduct product);
    void deleteProduct(String barcode);
}
