package com.example.bibilabo.scheduler;

import com.example.bibilabo.service.ExpiringProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiryCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiryCleanupScheduler.class);

    @Autowired
    private ExpiringProductService expiringProductService;

    @Scheduled(fixedRate = 300_000)
    public void cleanupExpiredProducts() {
        int count = expiringProductService.markExpiredProducts();
        if (count > 0) {
            log.info("Marked {} expired product(s) as DISCARDED", count);
        }
    }
}
