package com.ebrain.admin.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // 세션이 없거나, ADMIN_SESSION_ID가 없으면 로그인 페이지로
        if (session == null || session.getAttribute("ADMIN_SESSION_ID") == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
