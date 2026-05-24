package com.example.bibilabo.util;

import com.example.bibilabo.entity.StandardProduct;
import com.example.bibilabo.entity.Store;

import java.util.List;

public class I18nUtil {

    public static void applyStoreLocale(Store store, String lang) {
        if (store == null || lang == null) return;
        if ("en".equalsIgnoreCase(lang)) {
            if (store.getStoreNameEn() != null && !store.getStoreNameEn().isEmpty()) {
                store.setStoreName(store.getStoreNameEn());
            }
            if (store.getCityEn() != null && !store.getCityEn().isEmpty()) {
                store.setCity(store.getCityEn());
            }
            if (store.getAddressEn() != null && !store.getAddressEn().isEmpty()) {
                store.setAddress(store.getAddressEn());
            }
        }
    }

    public static void applyStoreLocale(List<Store> stores, String lang) {
        if (stores == null || lang == null) return;
        for (Store store : stores) {
            applyStoreLocale(store, lang);
        }
    }

    public static void applyProductLocale(StandardProduct product, String lang) {
        if (product == null || lang == null) return;
        if ("en".equalsIgnoreCase(lang)) {
            if (product.getProductNameEn() != null && !product.getProductNameEn().isEmpty()) {
                product.setProductName(product.getProductNameEn());
            }
        }
    }

    public static void applyProductLocale(List<StandardProduct> products, String lang) {
        if (products == null || lang == null) return;
        for (StandardProduct product : products) {
            applyProductLocale(product, lang);
        }
    }
}
