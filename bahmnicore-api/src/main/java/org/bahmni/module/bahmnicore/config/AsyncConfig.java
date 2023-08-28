//package org.bahmni.module.bahmnicore.config;
//
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Executor;
//@Configuration
//@EnableAsync
//public class AsyncConfig {
//
//    @Bean(name = "bahmniCoreAsync")
//    public Executor threadPoolTaskExecutor() {
//        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
//        return threadPoolTaskExecutor;
//    }
//}