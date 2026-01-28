package com.healthfamily.security;

import com.healthfamily.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends GenericFilterBean {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        
        // 检查是否为公共路径，如果是则直接跳过JWT验证
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = resolveToken(httpRequest);
        // 只有当请求头中包含有效token时才进行验证，否则继续执行过滤器链
        if (token != null && jwtUtil.validateToken(token)) {
            var claims = jwtUtil.parseToken(token);
            String subject = claims.getSubject();
            if (StringUtils.hasText(subject)) {
                try {
                    Long userId = Long.parseLong(subject);
                    userRepository.findById(userId)
                            .ifPresent(user -> {
                                var principal = new UserPrincipal(user);
                                var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        principal,
                                        null,
                                        principal.getAuthorities()
                                );
                                authentication.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(httpRequest));
                                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
                            });
                } catch (NumberFormatException ex) {
                    log.warn("Invalid JWT subject: {}", subject);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicPath(String path) {
        // 定义不需要JWT验证的公共路径
        return path.startsWith("/api/auth/") ||
               path.startsWith("/auth/") ||
               path.startsWith("/api/user/avatar/") ||
               path.startsWith("/user/avatar/") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}

