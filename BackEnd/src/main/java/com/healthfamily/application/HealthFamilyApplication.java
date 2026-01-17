package com.healthfamily.application;

import com.healthfamily.config.RecommendationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.healthfamily.application",
        "com.healthfamily.common",
        "com.healthfamily.config",
        "com.healthfamily.domain",
        "com.healthfamily.security",
        "com.healthfamily.service",
        "com.healthfamily.web",
        "com.healthfamily.ai"
})
@EnableScheduling
@EnableJpaRepositories(basePackages = {
        "com.healthfamily.domain.repository"
})
@EntityScan(basePackages = {
        "com.healthfamily.domain.entity"
})
@EnableJpaAuditing
@EnableConfigurationProperties(RecommendationProperties.class)
public class HealthFamilyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthFamilyApplication.class, args);
    }
}
