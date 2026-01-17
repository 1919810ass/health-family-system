package com.healthfamily.config;

import com.healthfamily.security.JwtAuthenticationFilter;
import com.healthfamily.security.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.core.annotation.Order;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 注入JWT过滤器（关键：之前缺失，导致认证逻辑不生效）
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    @Order(0) // 设置为Order(0)，确保优先于推荐模块的Order(1)执行，能够处理所有路径
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 启用CORS（使用唯一配置）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 禁用CSRF（JWT场景不需要）
                .csrf(csrf -> csrf.disable())
                // 无状态会话（JWT不依赖会话）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 禁用默认表单登录（避免重定向到/login）
                .formLogin(form -> form.disable())
                // 禁用HTTP Basic认证
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        // 公共路径，无需认证
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/auth/**", "/api/user/avatar/**").permitAll()
                        // 管理员接口
                        .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                        // 医生接口，仅DOCTOR角色可访问
                        .requestMatchers("/api/doctor/**").hasRole("DOCTOR")
                        // 家庭接口，ADMIN和FAMILY_ADMIN可访问
                        .requestMatchers("/api/families/**").hasAnyRole("ADMIN", "FAMILY_ADMIN", "DOCTOR", "MEMBER")
                        // 健康提醒接口
                        .requestMatchers("/api/reminders/todo").hasAnyRole("ADMIN", "FAMILY_ADMIN", "DOCTOR", "MEMBER")
                        .requestMatchers("/api/reminders/*/status").hasAnyRole("ADMIN", "FAMILY_ADMIN", "DOCTOR", "MEMBER")
                        .requestMatchers("/api/reminders").hasAnyRole("ADMIN", "FAMILY_ADMIN", "DOCTOR", "MEMBER")
                        .requestMatchers("/api/reminders/**").hasAnyRole("ADMIN", "FAMILY_ADMIN", "DOCTOR")
                        // 其他需要认证的API接口
                        .requestMatchers("/api/**").authenticated()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 唯一的CORS配置（覆盖所有路径）
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:5174");
        config.addAllowedOrigin("http://localhost:5175");
        config.addAllowedMethod("*"); // 允许所有HTTP方法
        config.addAllowedHeader("*"); // 允许所有请求头
        config.setAllowCredentials(true); // 允许携带Cookie
        config.setMaxAge(3600L); // 预检请求缓存时间

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 所有路径应用CORS
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }
}
