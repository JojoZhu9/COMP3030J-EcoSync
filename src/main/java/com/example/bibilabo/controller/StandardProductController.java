package com.example.bibilabo.controller;

import com.example.bibilabo.entity.StandardProduct;
import com.example.bibilabo.service.StandardProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "1. 标准商品库模块 (SPU)", description = "维护系统基础商品字典、条码及原价")
public class StandardProductController {

    @Autowired
    private StandardProductService productService;

    @GetMapping
    @Operation(summary = "获取所有标准商品", description = "查询基础商品库中的所有商品列表")
    public List<StandardProduct> getAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/{barcode}")
    @Operation(summary = "查询单个标准商品", description = "根据商品条码 (Barcode) 获取商品信息及原价")
    public StandardProduct getByBarcode(@Parameter(description = "商品条码") @PathVariable String barcode) {
        return productService.getProductByBarcode(barcode);
    }

    @PostMapping
    @Operation(summary = "录入新标准商品", description = "在基础库中添加新的商品品类")
    public String add(@RequestBody StandardProduct product) {
        productService.createProduct(product);
        return "商品添加成功，条码: " + product.getBarcode();
    }

    @PutMapping("/{barcode}")
    @Operation(summary = "更新标准商品", description = "修改已有商品的名称或正常售价")
    public String update(@Parameter(description = "商品条码") @PathVariable String barcode, @RequestBody StandardProduct product) {
        product.setBarcode(barcode);
        productService.updateProduct(product);
        return "商品更新成功";
    }

    @DeleteMapping("/{barcode}")
    @Operation(summary = "删除标准商品", description = "从基础库中彻底移除该商品（注意可能受外键约束影响）")
    public String delete(@Parameter(description = "商品条码") @PathVariable String barcode) {
        productService.deleteProduct(barcode);
        return "商品删除成功";
    }
}