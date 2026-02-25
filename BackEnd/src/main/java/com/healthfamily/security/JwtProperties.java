package com.healthfamily.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWTProperties安全组件
 * <p>
 * 负责认证鉴权、Token 解析、权限校验或安全相关的辅助能力。
 * </p>
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        String issuer,
        long accessTokenValidityMinutes
) {
}

