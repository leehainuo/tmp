package com.miku.redis.annotation;

import java.lang.annotation.*;

/**
 * 防重复提交注解
 * <p>
 * 基于 Redis 实现分布式防重复提交，支持 RedisTemplate 和 Redisson 两种实现方式
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 *   // 5秒内不允许重复提交
 *   @RepeatSubmit(interval = 5000)
 *   public Result<Void> createOrder(@RequestBody OrderDTO order) {
 *       // ...
 *   }
 *
 *   // 自定义提示消息
 *   @RepeatSubmit(interval = 3000, message = "请勿重复下单")
 *   public Result<Void> placeOrder(@RequestBody OrderDTO order) {
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
public @interface RepeatSubmit {

    /**
     * 间隔时间（毫秒），在此时间内不允许重复提交
     */
    int interval() default 5000;

    /**
     * 提示消息
     */
    String message() default "请勿重复提交";

    /**
     * 是否包含请求参数（用于区分不同的请求）
     */
    boolean includeParams() default true;
}

