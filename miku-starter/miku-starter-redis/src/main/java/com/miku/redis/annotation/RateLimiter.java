package com.miku.redis.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * <p>
 * 基于 Redis 实现分布式限流，支持 RedisTemplate + Lua 和 Redisson 两种实现方式
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 *   // 限制每秒最多10次请求
 *   @RateLimiter(key = "login", time = 1, count = 10)
 *   public Result<LoginResponse> login(@RequestBody LoginRequest request) {
 *       // ...
 *   }
 *
 *   // 限制每个IP每分钟最多5次请求
 *   @RateLimiter(key = "sms", time = 60, count = 5, limitType = LimitType.IP)
 *   public Result<Void> sendSms(@RequestParam String phone) {
 *       // ...
 *   }
 * }
 * </pre>
 *
 * @author miku
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流key
     * 支持 SpEL 表达式
     */
    String key() default "";

    /**
     * 限流时间窗口，单位：秒
     */
    int time() default 60;

    /**
     * 时间窗口内最大请求次数
     */
    int count() default 100;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 默认策略全局限流
         */
        DEFAULT,

        /**
         * 根据请求者IP进行限流
         */
        IP,

        /**
         * 根据用户进行限流
         */
        USER
    }
}

