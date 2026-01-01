package com.miku.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步任务配置
 * 用于支持操作日志异步记录
 *
 * @author lihainuo.com
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // 使用默认的异步线程池配置
    // 如需自定义线程池，可以重写 getAsyncExecutor() 方法
}

