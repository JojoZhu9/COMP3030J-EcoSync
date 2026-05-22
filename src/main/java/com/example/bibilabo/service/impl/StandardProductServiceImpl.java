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
        productMapper.update(product);
        return CompletableFuture.completedFuture("Product updated successfully");
    }

    @Override
    public void deleteProduct(String barcode) {
        expiringProductMapper.deleteByBarcode(barcode);
        productMapper.deleteByBarcode(barcode);
    }
}
