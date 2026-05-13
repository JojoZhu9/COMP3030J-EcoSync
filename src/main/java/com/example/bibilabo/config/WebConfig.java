package com.example.bibilabo.config;

import com.example.bibilabo.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry; // 新增导入
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    // --- 新增跨域配置开始 ---
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有路径
                .allowedOrigins(
                    "http://localhost:5173",
                    "http://127.0.0.1:5173",
                    "http://csi420-02-vm9.ucd.ie",
                    "https://csi420-02-vm9.ucd.ie"
                ) // 允许本地开发和线上域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的方法
                .allowedHeaders("*") // 允许的 Header
                .allowCredentials(true); // 允许带上 Cookie 或认证信息
    }
    // --- 新增跨域配置结束 ---

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                // 在这里新增放行 uploads 路径
                .excludePathPatterns(
                        "/api/users/login",
                        "/api/users",
                        "/api/products",
                        "/api/uploads/**"  // 关键：放行图片资源
                );
    }
}