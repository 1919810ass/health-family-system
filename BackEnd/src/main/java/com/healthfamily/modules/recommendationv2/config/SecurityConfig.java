package com.healthfamily.modules.recommendationv2.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置配置类
 * <p>
 * 集中定义框架与组件的装配、参数及运行时行为（如安全、异步、HTTP等）。
 * </p>
 */
@Configuration("recommendationSecurityConfig")
public class SecurityConfig {
  @Bean(name = "recommendationSecurityFilterChain")
  @Order(1)
  public SecurityFilterChain recommendationSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/recommendation/**")  // 限制此安全配置只应用于推荐模块路径
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
            ).permitAll()
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }
}
