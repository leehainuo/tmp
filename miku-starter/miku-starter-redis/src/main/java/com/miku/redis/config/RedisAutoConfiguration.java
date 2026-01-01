package com.miku.redis.config;

import com.miku.redis.aspect.RateLimiterAspect;
import com.miku.redis.aspect.RepeatSubmitAspect;
import com.miku.redis.util.RedissonLockUtil;
import com.miku.redis.util.RedissonRateLimiterUtil;
import com.miku.redis.util.RedissonSemaphoreUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis 自动配置
 * <p>
 * 统一管理限流、防重复提交、Redisson 等功能
 *
 * @author miku
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisTemplate.class)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {

    private final BeanFactory beanFactory;
    private final RedisProperties properties;

    public RedisAutoConfiguration(BeanFactory beanFactory, RedisProperties properties) {
        this.beanFactory = beanFactory;
        this.properties = properties;
    }

    /**
     * Redis 可用性检查监听器
     * 在应用上下文完全初始化后检查 Redis 是否可用，并输出相应的日志提示
     */
    @Bean
    @ConditionalOnClass(RedisTemplate.class)
    public ApplicationListener<ContextRefreshedEvent> redisAvailabilityChecker() {
        return event -> {
            try {
                // 使用 ResolvableType 处理泛型类型，解决类型擦除问题
                ResolvableType redisTemplateType = ResolvableType.forClassWithGenerics(
                        RedisTemplate.class, String.class, Object.class);
                ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider =
                        beanFactory.getBeanProvider(redisTemplateType);
                RedisTemplate<String, Object> redisTemplate = redisTemplateProvider.getIfAvailable();

                if (redisTemplate == null) {
                    // Redis 不可用，输出提示日志
                    boolean rateLimitEnabled = properties.getRateLimit() != null
                            && properties.getRateLimit().isEnabled();
                    boolean repeatSubmitEnabled = properties.getRepeatSubmit() != null
                            && properties.getRepeatSubmit().isEnabled();

                    if (rateLimitEnabled || repeatSubmitEnabled) {
                        StringBuilder message = new StringBuilder("[Miku-Redis] Redis 不可用，以下功能将不可用：");
                        if (rateLimitEnabled) {
                            message.append(" 限流");
                        }
                        if (repeatSubmitEnabled) {
                            message.append(" 防重复提交");
                        }
                        log.warn(message.toString());
                    }
                } else {
                    // Redis 可用，输出成功日志
                    log.info("[Miku-Redis] Redis 可用，限流和防重复提交功能已启用");
                }
            } catch (Exception e) {
                // 忽略异常，不影响应用启动
                log.debug("[Miku-Redis] 检查 Redis 可用性时发生异常: {}", e.getMessage());
            }
        };
    }

    /**
     * 限流切面
     * <p>
     * 注意：限流功能需要 Redis，如果 Redis 不可用，此 Bean 不会被创建
     */
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnProperty(prefix = "spring.data.redis.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public RateLimiterAspect rateLimiterAspect(
            RedisTemplate<String, Object> redisTemplate,
            RedisProperties properties,
            BeanFactory beanFactory) {
        // 使用反射查找 RedissonClient，避免类加载时依赖
        Object redissonClient = getRedissonClientIfAvailable(beanFactory);
        return new RateLimiterAspect(redisTemplate, properties, redissonClient);
    }

    /**
     * 防重复提交切面
     * <p>
     * 注意：防重复提交功能需要 Redis，如果 Redis 不可用，此 Bean 不会被创建
     */
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnProperty(prefix = "spring.data.redis.repeat-submit", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public RepeatSubmitAspect repeatSubmitAspect(
            RedisTemplate<String, Object> redisTemplate,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper,
            RedisProperties properties,
            BeanFactory beanFactory) {
        // 使用反射查找 RedissonClient，避免类加载时依赖
        Object redissonClient = getRedissonClientIfAvailable(beanFactory);
        return new RepeatSubmitAspect(redisTemplate, objectMapper, properties, redissonClient);
    }

    /**
     * Redisson 分布式锁工具类
     */
    @Bean
    @ConditionalOnClass(name = "org.redisson.api.RedissonClient")
    @ConditionalOnMissingBean
    public RedissonLockUtil redissonLockUtil(BeanFactory beanFactory) {
        Object redissonClient = getRedissonClientIfAvailable(beanFactory);
        return new RedissonLockUtil(redissonClient);
    }

    /**
     * Redisson 信号量工具类
     */
    @Bean
    @ConditionalOnClass(name = "org.redisson.api.RedissonClient")
    @ConditionalOnMissingBean
    public RedissonSemaphoreUtil redissonSemaphoreUtil(BeanFactory beanFactory) {
        Object redissonClient = getRedissonClientIfAvailable(beanFactory);
        return new RedissonSemaphoreUtil(redissonClient);
    }

    /**
     * Redisson 限流器工具类
     */
    @Bean
    @ConditionalOnClass(name = "org.redisson.api.RedissonClient")
    @ConditionalOnMissingBean
    public RedissonRateLimiterUtil redissonRateLimiterUtil(BeanFactory beanFactory) {
        Object redissonClient = getRedissonClientIfAvailable(beanFactory);
        return new RedissonRateLimiterUtil(redissonClient);
    }

    /**
     * 获取 RedissonClient（如果可用）
     * 使用反射避免类加载时依赖 RedissonClient 类型
     */
    private Object getRedissonClientIfAvailable(BeanFactory beanFactory) {
        try {
            // 尝试通过类名查找 RedissonClient bean
            Class<?> redissonClientClass = Class.forName("org.redisson.api.RedissonClient");
            ObjectProvider<?> provider = beanFactory.getBeanProvider(redissonClientClass);
            return provider.getIfAvailable();
        } catch (ClassNotFoundException e) {
            // Redisson 未引入，返回 null
            return null;
        } catch (Exception e) {
            // 其他异常，返回 null
            return null;
        }
    }
}

