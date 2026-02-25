package com.healthfamily.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP配置配置类
 * <p>
 * 集中定义框架与组件的装配、参数及运行时行为（如安全、异步、HTTP等）。
 * </p>
 */
@Configuration
public class HttpConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}