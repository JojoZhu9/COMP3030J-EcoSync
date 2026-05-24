package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.StandardProduct;
import com.example.bibilabo.mapper.ExpiringProductMapper;
import com.example.bibilabo.mapper.StandardProductMapper;
import com.example.bibilabo.service.StandardProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class StandardProductServiceImpl implements StandardProductService {

    @Autowired
    private StandardProductMapper productMapper;

    @Autowired
    private ExpiringProductMapper expiringProductMapper;

    @Override
    public List<StandardProduct> getAllProducts() {
        return productMapper.findAll();
    }

    @Override
    public StandardProduct getProductByBarcode(String barcode) {
        return productMapper.findByBarcode(barcode);
    }

    @Override
    public void createProduct(StandardProduct product) {
        productMapper.insert(product);
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<String> updateProduct(StandardProduct product) {
        // 先查询原始数据，避免 null 字段覆盖已有值
        StandardProduct existing = productMapper.findByBarcode(product.getBarcode());
        if (existing != null) {
            if (product.getProductName() == null) {
                product.setProductName(existing.getProductName());
            }
            if (product.getProductNameEn() == null) {
                product.setProductNameEn(existing.getProductNameEn());
            }
            if (product.getNormalPrice() == null) {
                product.setNormalPrice(existing.getNormalPrice());
            }
            if (product.getDiscountRates() == null) {
                product.setDiscountRates(existing.getDiscountRates());
            }
            if (product.getImageUrl() == null) {
                product.setImageUrl(existing.getImageUrl());
            }
        }
        productMapper.update(product);
        return CompletableFuture.completedFuture("Product updated successfully");
    }

    @Override
    public void deleteProduct(String barcode) {
        expiringProductMapper.deleteByBarcode(barcode);
        productMapper.deleteByBarcode(barcode);
    }
}
