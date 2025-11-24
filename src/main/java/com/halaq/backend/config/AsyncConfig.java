package com.halaq.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
// Active la capacité de Spring à exécuter des méthodes @Async en arrière-plan
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);      // Nombre de threads toujours actifs
        executor.setMaxPoolSize(5);       // Nombre maximum de threads
        executor.setQueueCapacity(100);   // Tâches en attente si tous les threads sont occupés
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}