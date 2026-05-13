package com.example.bibilabo.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadsInitializer {

    private static final Logger log = LoggerFactory.getLogger(UploadsInitializer.class);

    @Value("${app.upload.dir:./uploads/products}")
    private String uploadDir;

    @PostConstruct
    public void copySeedImages() throws IOException {
        Path targetDir = Paths.get(uploadDir);
        Files.createDirectories(targetDir);
        log.info("Uploads directory ensured: {}", targetDir.toAbsolutePath());

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/static/uploads/products/*.jpg");

        int copied = 0;
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) continue;
            Path target = targetDir.resolve(filename);
            if (Files.exists(target)) continue; // don't overwrite existing files

            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, target);
                copied++;
            }
        }
        if (copied > 0) {
            log.info("Copied {} seed images to uploads directory", copied);
        }
    }
}
