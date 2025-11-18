package com.devnovus.oneBox.global.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long start = System.currentTimeMillis();

        // 요청 정보
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIp(request);

        // 요청 로그
        log.info("[REQ] {} {}{} | IP: {} | UA: {}", method, uri, query, clientIp, userAgent);

        // 다음 필터 실행
        filterChain.doFilter(request, response);

        long duration = System.currentTimeMillis() - start;

        // 응답 로그
        log.info("[RES] {} {} | Status: {} | Duration: {}ms", method, uri, response.getStatus(), duration);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null) {
            return forwarded.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
