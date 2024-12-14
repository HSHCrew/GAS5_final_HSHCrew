package org.zerock.Altari.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // 비동기 처리 활성화
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);       // 기본적으로 유지할 스레드 수
        executor.setMaxPoolSize(30);       // 최대 스레드 수
        executor.setQueueCapacity(100);     // 대기 작업 큐 크기
        executor.setThreadNamePrefix("Async-"); // 스레드 이름 접두사
        executor.initialize();            // 스레드 풀 초기화
        return executor;
    }
}
