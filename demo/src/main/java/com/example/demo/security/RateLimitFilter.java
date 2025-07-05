package com.example.demo.security;

import com.bucket4j.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_CACHE = "rateLimit";
    private static final int CAPACITY = 100; // Số lượng request tối đa
    private static final int REFILL_TOKENS = 100; // Số token được nạp lại
    private static final int DURATION_MINUTES = 1; // Thời gian tính bằng phút

    private final CacheManager cacheManager;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Bỏ qua rate limit cho các endpoint công khai
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") || 
               path.startsWith("/v3/api-docs") || 
               path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIP(request);
        Cache cache = cacheManager.getCache(RATE_LIMIT_CACHE);
        
        if (cache == null) {
            log.error("Cache '{}' not found", RATE_LIMIT_CACHE);
            filterChain.doFilter(request, response);
            return;
        }
        
        Bucket bucket = cache.get(clientIp, Bucket.class);
        
        if (bucket == null) {
            Bandwidth limit = Bandwidth.simple(CAPACITY, Duration.ofMinutes(DURATION_MINUTES));
            bucket = Bucket.builder()
                    .addLimit(limit)
                    .build();
            cache.put(clientIp, bucket);
        }
        
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // Thêm các header thông tin về rate limit
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-Rate-Limit-Limit", String.valueOf(CAPACITY));
            response.addHeader("X-Rate-Limit-Reset", String.valueOf(TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill())));
            
            filterChain.doFilter(request, response);
        } else {
            // Trả về lỗi 429 (Too Many Requests) khi vượt quá giới hạn
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
            errorDetails.put("error", "Too Many Requests");
            errorDetails.put("message", "You have exhausted your API Request Quota");
            errorDetails.put("retryAfter", TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + " seconds");
            
            new ObjectMapper().writeValue(response.getWriter(), errorDetails);
            
            log.warn("Rate limit exceeded for IP: {}", clientIp);
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", 
                          "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", 
                          "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA"};
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0];
            }
        }
        
        return request.getRemoteAddr();
    }
}
