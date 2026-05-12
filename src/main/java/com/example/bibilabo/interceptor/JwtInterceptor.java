package com.example.bibilabo.interceptor;

import com.example.bibilabo.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果是跨域的 OPTIONS 请求，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 从请求头获取 Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return false;
        }

        // 2. 提取并验证 Token
        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtils.parseToken(token);

            // 3. 将解析出的用户信息存入 request 属性中，方便后续 Controller 使用
            request.setAttribute("currentUserId", claims.get("userId"));
            request.setAttribute("currentUserRole", claims.get("role"));

            // 🔥 RBAC 粗粒度权限拦截示例：
            // 如果请求路径包含 "/expiring-products" 且是 POST/PUT/DELETE 请求，必须是员工或管理员
            String uri = request.getRequestURI();
            String role = (String) claims.get("role");
            String method = request.getMethod();

            if (uri.contains("/expiring-products") && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
                if ("CONSUMER".equals(role)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Access Denied: You don't have permission to modify products.");
                    return false;
                }
            }

            return true; // 放行
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired Token");
            return false;
        }
    }
}