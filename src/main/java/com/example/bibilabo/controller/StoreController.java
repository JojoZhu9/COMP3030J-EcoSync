package com.example.bibilabo.controller;

import com.example.bibilabo.entity.Store;
import com.example.bibilabo.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@Tag(name = "7. 门店配置模块", description = "维护 7-ELEVEn 线下门店网点信息")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @GetMapping
    @Operation(summary = "获取所有门店", description = "前端地图展示或下拉框选择：列出所有支持临期售卖的门店")
    public List<Store> getAll() {
        return storeService.getAllStores();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询单家门店详情", description = "根据门店ID获取地址及所在城市")
    public Store getById(@Parameter(description = "门店ID") @PathVariable("id") Integer id) {
        return storeService.getStoreById(id);
    }

    @PostMapping
    @Operation(summary = "新增门店", description = "开通新门店的临期售卖权限")
    public String add(@RequestBody Store store) {
        return storeService.createStore(store);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新门店信息", description = "修改门店名称或搬迁地址修改")
    public String update(@Parameter(description = "门店ID") @PathVariable("id") Integer id, @RequestBody Store store) {
        store.setStoreId(id);
        return storeService.updateStore(store);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除门店", description = "关停门店系统权限")
    public String delete(@Parameter(description = "门店ID") @PathVariable("id") Integer id) {
        return storeService.deleteStore(id);
    }
}