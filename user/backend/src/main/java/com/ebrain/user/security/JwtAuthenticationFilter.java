package com.ebrain.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Bearer 토큰 추출
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // 토큰 검증
            if (jwtUtil.validateToken(token)) {
                String memberId = jwtUtil.extractMemberId(token);
                String memberName = jwtUtil.extractMemberName(token);

                // Request Attribute에 저장 (Controller에서 사용)
                request.setAttribute("memberId", memberId);
                request.setAttribute("memberName", memberName);
            }
        }

        filterChain.doFilter(request, response);
    }
}
