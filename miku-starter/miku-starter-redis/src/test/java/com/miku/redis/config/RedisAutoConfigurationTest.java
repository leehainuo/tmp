package com.miku.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miku.redis.aspect.RateLimiterAspect;
import com.miku.redis.aspect.RepeatSubmitAspect;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis 自动配置测试
 *
 * 验证在不同条件下，限流和防重复提交切面是否按预期创建或跳过。
 */
@SpringBootTest(classes = RedisAutoConfigurationTest.DefaultConfig.class)
class RedisAutoConfigurationTest {

    /**
     * 默认配置：存在 RedisTemplate，功能开关均为开启（默认值）
     */
    @Configuration
    @ImportAutoConfiguration(RedisAutoConfiguration.class)
    static class DefaultConfig {

        @Bean
        RedisTemplate<String, Object> redisTemplate() {
            // 使用 Mockito 避免真实 Redis 连接
            return Mockito.mock(RedisTemplate.class);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Test
    void shouldCreateRateLimiterAspectWhenRedisAvailable(ApplicationContext context) {
        assertThat(context.getBeansOfType(RateLimiterAspect.class)).isNotEmpty();
    }

    @Test
    void shouldCreateRepeatSubmitAspectWhenRedisAvailable(ApplicationContext context) {
        assertThat(context.getBeansOfType(RepeatSubmitAspect.class)).isNotEmpty();
    }

    /**
     * 配置关闭限流功能时，不应创建 RateLimiterAspect。
     */
    @Nested
    @SpringBootTest(
            classes = RedisAutoConfigurationTest.RateLimitDisabledTest.RateLimitDisabledConfig.class,
            properties = "spring.data.redis.rate-limit.enabled=false"
    )
    class RateLimitDisabledTest {

        @Configuration
        @ImportAutoConfiguration(RedisAutoConfiguration.class)
        static class RateLimitDisabledConfig {

            @Bean
            RedisTemplate<String, Object> redisTemplate() {
                return Mockito.mock(RedisTemplate.class);
            }

            @Bean
            ObjectMapper objectMapper() {
                return new ObjectMapper();
            }
        }

        @Test
        void shouldNotCreateRateLimiterAspectWhenDisabled(ApplicationContext context) {
            assertThat(context.getBeansOfType(RateLimiterAspect.class)).isEmpty();
        }
    }

    /**
     * 配置关闭防重复提交功能时，不应创建 RepeatSubmitAspect。
     */
    @Nested
    @SpringBootTest(
            classes = RedisAutoConfigurationTest.RepeatSubmitDisabledTest.RepeatSubmitDisabledConfig.class,
            properties = "spring.data.redis.repeat-submit.enabled=false"
    )
    class RepeatSubmitDisabledTest {

        @Configuration
        @ImportAutoConfiguration(RedisAutoConfiguration.class)
        static class RepeatSubmitDisabledConfig {

            @Bean
            RedisTemplate<String, Object> redisTemplate() {
                return Mockito.mock(RedisTemplate.class);
            }

            @Bean
            ObjectMapper objectMapper() {
                return new ObjectMapper();
            }
        }

        @Test
        void shouldNotCreateRepeatSubmitAspectWhenDisabled(ApplicationContext context) {
            assertThat(context.getBeansOfType(RepeatSubmitAspect.class)).isEmpty();
        }
    }
}



