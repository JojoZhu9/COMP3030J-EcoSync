package com.example.bibilabo.service.impl;

import com.example.bibilabo.entity.Store;
import com.example.bibilabo.mapper.StoreMapper;
import com.example.bibilabo.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreMapper storeMapper;

    @Override
    public List<Store> getAllStores() {
        return storeMapper.findAll();
    }

    @Override
    public Store getStoreById(Integer storeId) {
        return storeMapper.findById(storeId);
    }

    @Override
    public String createStore(Store store) {
        storeMapper.insert(store);
        return "Store added successfully, store ID: " + store.getStoreId();
    }

    @Override
    public String updateStore(Store store) {
        // 先查询原始数据，避免 null 字段覆盖已有值
        Store existing = storeMapper.findById(store.getStoreId());
        if (existing != null) {
            if (store.getStoreName() == null) store.setStoreName(existing.getStoreName());
            if (store.getStoreNameEn() == null) store.setStoreNameEn(existing.getStoreNameEn());
            if (store.getCity() == null) store.setCity(existing.getCity());
            if (store.getCityEn() == null) store.setCityEn(existing.getCityEn());
            if (store.getAddress() == null) store.setAddress(existing.getAddress());
            if (store.getAddressEn() == null) store.setAddressEn(existing.getAddressEn());
            if (store.getLatitude() == null) store.setLatitude(existing.getLatitude());
            if (store.getLongitude() == null) store.setLongitude(existing.getLongitude());
        }
        storeMapper.update(store);
        return "Store updated successfully";
    }

    @Override
    public String deleteStore(Integer storeId) {
        storeMapper.deleteById(storeId);
        return "Store deleted successfully";
    }
}
