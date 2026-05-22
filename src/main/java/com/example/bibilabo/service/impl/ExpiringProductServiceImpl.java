package com.example.bibilabo.service.impl;

import com.example.bibilabo.constant.ProductStatus; // 引入常量
import com.example.bibilabo.entity.ExpiringProduct;
import com.example.bibilabo.mapper.ExpiringProductMapper;
import com.example.bibilabo.service.ExpiringProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ExpiringProductServiceImpl implements ExpiringProductService {

    @Autowired
    private ExpiringProductMapper expiringProductMapper;

    @Override
    public List<ExpiringProduct> getAllProducts() {
        return expiringProductMapper.findAll();
    }

    @Override
    public ExpiringProduct getProductById(Integer productId) {
        return expiringProductMapper.findById(productId);
    }

    @Override
    public List<ExpiringProduct> getAvailableProductsByStore(Integer storeId) {
        return expiringProductMapper.findAvailableByStore(storeId);
    }

    @Override
    public String createProduct(ExpiringProduct product) {
        // 使用常量接口替换硬编码
        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.AVAILABLE);
        }
        expiringProductMapper.insert(product);
        return "Product listed successfully, batch ID: " + product.getProductId();
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<String> updateProduct(ExpiringProduct product) {
        expiringProductMapper.update(product);
        return CompletableFuture.completedFuture("Product updated successfully");
    }

    @Override
    public String deleteProduct(Integer productId) {
        expiringProductMapper.deleteById(productId);
        return "Product deleted successfully";
    }

    @Override
    public boolean decreaseStock(Integer productId, Integer quantity) {
        int affectedRows = expiringProductMapper.decreaseStock(productId, quantity);
        return affectedRows > 0; // 如果返回值大于0，说明库存充足且扣减成功
    }
}
