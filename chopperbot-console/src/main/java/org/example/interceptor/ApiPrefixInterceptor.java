package org.example.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author Genius
 * @date 2023/09/11 00:38
 **/
public class ApiPrefixInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/appApi")) {
            // 去除请求前缀 "/api"
            String newRequestURI = requestURI.replace("/appApi","");
            request.getRequestDispatcher(newRequestURI).forward(request, response);
            return false; // 不继续处理原始请求
        }
        return true; // 继续处理原始请求
    }
    // 其他方法省略...
}
