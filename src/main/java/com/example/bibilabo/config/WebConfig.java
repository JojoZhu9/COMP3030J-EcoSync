package com.example.bibilabo.config;

import com.example.bibilabo.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**") // 拦截所有 /api 开头的请求
                .excludePathPatterns("/api/users/login", "/api/users", "/api/products"); // 排除登录、注册、查看通用商品等不需要 Token 的接口
    }
}