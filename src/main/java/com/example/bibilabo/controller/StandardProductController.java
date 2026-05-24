package com.example.bibilabo.controller;

import com.example.bibilabo.entity.StandardProduct;
import com.example.bibilabo.service.MinioService;
import com.example.bibilabo.service.StandardProductService;
import com.example.bibilabo.util.I18nUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/products")
@Tag(name = "1. 标准商品库模块 (SPU)", description = "维护系统基础商品字典、条码及原价")
public class StandardProductController {

    @Autowired
    private StandardProductService productService;

    @Autowired
    private MinioService minioService;

    @GetMapping
    @Operation(summary = "获取所有标准商品", description = "查询基础商品库中的所有商品列表")
    public List<StandardProduct> getAll(@RequestParam(required = false, defaultValue = "zh") String lang) {
        List<StandardProduct> products = productService.getAllProducts();
        I18nUtil.applyProductLocale(products, lang);
        return products;
    }

    @GetMapping("/{barcode}")
    @Operation(summary = "查询单个标准商品", description = "根据商品条码 (Barcode) 获取商品信息及原价")
    public StandardProduct getByBarcode(@Parameter(description = "商品条码") @PathVariable String barcode,
                                         @RequestParam(required = false, defaultValue = "zh") String lang) {
        StandardProduct product = productService.getProductByBarcode(barcode);
        I18nUtil.applyProductLocale(product, lang);
        return product;
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "录入新标准商品（支持图片上传）", description = "在基础库中添加新的商品品类，可选上传商品图片")
    public String add(
            @RequestParam("barcode") String barcode,
            @RequestParam("product_name") String productName,
            @RequestParam(value = "product_name_en", required = false) String productNameEn,
            @RequestParam("normal_price") BigDecimal normalPrice,
            @RequestParam("discount_rates") String discountRates,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {

        StandardProduct product = new StandardProduct();
        product.setBarcode(barcode);
        product.setProductName(productName);
        product.setProductNameEn(productNameEn);
        product.setNormalPrice(normalPrice);
        product.setDiscountRates(discountRates);

        if (image != null && !image.isEmpty()) {
            String filename = barcode + "_" + UUID.randomUUID().toString().substring(0, 8)
                    + getExtension(image.getOriginalFilename());
            minioService.upload(image, filename);
            product.setImageUrl(filename);
        }

        productService.createProduct(product);
        return "Product added successfully, barcode: " + barcode;
    }

    @PutMapping("/{barcode}")
    @Operation(summary = "更新标准商品", description = "修改已有商品的名称或正常售价")
    public CompletableFuture<String> update(@Parameter(description = "商品条码") @PathVariable String barcode, @RequestBody StandardProduct product) {
        product.setBarcode(barcode);
        return productService.updateProduct(product);
    }

    @DeleteMapping("/{barcode}")
    @Operation(summary = "删除标准商品", description = "从基础库中彻底移除该商品（注意可能受外键约束影响）")
    public String delete(@Parameter(description = "商品条码") @PathVariable String barcode) {
        productService.deleteProduct(barcode);
        return "Product deleted successfully";
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}
