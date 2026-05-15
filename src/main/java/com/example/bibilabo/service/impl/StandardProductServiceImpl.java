package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.StandardProduct;
import com.example.bibilabo.mapper.StandardProductMapper;
import com.example.bibilabo.service.StandardProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StandardProductServiceImpl implements StandardProductService {

    @Autowired
    private StandardProductMapper productMapper;

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
        // 🔥 新增：创建商品时，默认设置状态为 'ACTIVE'
        if (product.getStatus() == null) {
            product.setStatus("ACTIVE");
        }
        productMapper.insert(product);
    }

    @Override
    public void updateProduct(StandardProduct product) {
        // 🔥 新增：防止普通更新时把状态置空
        if (product.getStatus() == null) {
            product.setStatus("ACTIVE");
        }
        productMapper.update(product);
    }

    @Override
    public void deleteProduct(String barcode) {
        // 这里的调用不需要变，因为 Mapper 里的 SQL 已经被我们改成 UPDATE 软删除了！
        productMapper.deleteByBarcode(barcode);
    }
}