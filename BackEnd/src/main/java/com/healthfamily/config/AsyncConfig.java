package com.healthfamily.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
/**
 * Async配置配置类
 * <p>
 * 集中定义框架与组件的装配、参数及运行时行为（如安全、异步、HTTP等）。
 * </p>
 */
@EnableAsync
public class AsyncConfig {

    @Bean(name = "reportExecutor")
    public ThreadPoolTaskExecutor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cpu = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(Math.max(2, cpu));
        executor.setMaxPoolSize(Math.max(4, cpu + 2));
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("report-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        // Use CallerRunsPolicy to handle overload gracefully
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
