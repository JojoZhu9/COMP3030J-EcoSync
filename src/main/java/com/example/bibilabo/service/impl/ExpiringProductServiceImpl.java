package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.ExpiringProduct;
import com.example.bibilabo.mapper.ExpiringProductMapper;
import com.example.bibilabo.service.ExpiringProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        // 如果前端没有传状态，默认设为 AVAILABLE
        if (product.getStatus() == null) {
            product.setStatus("AVAILABLE");
        }
        expiringProductMapper.insert(product);
        return "临期商品上架成功，批次ID: " + product.getProductId();
    }

    @Override
    public String updateProduct(ExpiringProduct product) {
        expiringProductMapper.update(product);
        return "临期商品信息更新成功";
    }

    @Override
    public String deleteProduct(Integer productId) {
        expiringProductMapper.deleteById(productId);
        return "临期商品删除成功";
    }

    @Override
    public boolean decreaseStock(Integer productId, Integer quantity) {
        int affectedRows = expiringProductMapper.decreaseStock(productId, quantity);
        return affectedRows > 0; // 如果返回值大于0，说明库存充足且扣减成功
    }
}