package com.example.bibilabo.service;

import com.example.bibilabo.entity.Store;
import java.util.List;

public interface StoreService {
    List<Store> getAllStores();
    Store getStoreById(Integer storeId);
    String createStore(Store store);
    String updateStore(Store store);
    String deleteStore(Integer storeId);
}