package com.example.bibilabo.config;

import com.example.bibilabo.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://csi420-02-vm9.ucd.ie",
                        "https://csi420-02-vm9.ucd.ie"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 修改点 1：将资源处理路径改为 /api/uploads/**，确保和前端请求以及拦截器放行路径一致
        // 这样访问 /api/uploads/products/1.jpg 就会去 /app/uploads/products/1.jpg 找
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:/app/uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/users/login",
                        "/api/users",
                        "/api/products",
                        "/api/uploads/**"  // 修改点 2：这里已经正确，保持不动
                );
    }
}