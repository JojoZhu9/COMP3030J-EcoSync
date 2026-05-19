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
        storeMapper.update(store);
        return "Store updated successfully";
    }

    @Override
    public String deleteStore(Integer storeId) {
        storeMapper.deleteById(storeId);
        return "Store deleted successfully";
    }
}