package com.example.bibilabo.controller;

import com.example.bibilabo.entity.ExpiringProduct;
import com.example.bibilabo.service.ExpiringProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expiring-products")
@Tag(name = "3. 临期库存管理模块 (SKU)", description = "管理门店实际上架的具体临期商品实例、库存及到期时间")
public class ExpiringProductController {

    @Autowired
    private ExpiringProductService expiringProductService;

    @GetMapping
    @Operation(summary = "获取所有临期商品", description = "后台管理用：查询所有门店的临期商品明细")
    public List<ExpiringProduct> getAll() {
        return expiringProductService.getAllProducts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情", description = "根据具体批次ID查询某件临期商品的详情")
    public ExpiringProduct getById(@Parameter(description = "商品批次ID") @PathVariable("id") Integer id) {
        return expiringProductService.getProductById(id);
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "查询门店在售商品", description = "消费者端主接口：获取指定门店当前状态为 AVAILABLE 的临期商品")
    public List<ExpiringProduct> getAvailableByStore(@Parameter(description = "门店ID") @PathVariable("storeId") Integer storeId) {
        return expiringProductService.getAvailableProductsByStore(storeId);
    }

    @PostMapping
    @Operation(summary = "上架临期商品", description = "店员扫码录入新的临期商品（需绑定条码、门店和确切过期时间）")
    public String add(@RequestBody ExpiringProduct product) {
        return expiringProductService.createProduct(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新临期商品状态", description = "手动修改库存量或将状态改为报损/售罄")
    public String update(@Parameter(description = "商品批次ID") @PathVariable("id") Integer id, @RequestBody ExpiringProduct product) {
        product.setProductId(id);
        return expiringProductService.updateProduct(product);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除临期商品记录", description = "通常因录入错误才调用删除，一般建议用更新状态替代")
    public String delete(@Parameter(description = "商品批次ID") @PathVariable("id") Integer id) {
        return expiringProductService.deleteProduct(id);
    }
}