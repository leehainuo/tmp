package com.miku.redis.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis 配置属性绑定测试。
 *
 * 验证 application 配置能正确绑定到 {@link RedisProperties}。
 */
@SpringBootTest(
        classes = RedisPropertiesBindingTest.TestConfig.class,
        properties = {
                "spring.data.redis.rate-limit.enabled=false",
                "spring.data.redis.rate-limit.implementation=redisson",
                "spring.data.redis.repeat-submit.enabled=false",
                "spring.data.redis.repeat-submit.implementation=redis-template",
                "spring.data.redis.redisson.enabled=true"
        }
)
class RedisPropertiesBindingTest {

    @Configuration
    @ImportAutoConfiguration(RedisAutoConfiguration.class)
    static class TestConfig {
        // 仅导入自动配置与配置属性
    }

    @Autowired
    private RedisProperties properties;

    @Test
    void shouldBindRedisPropertiesFromConfiguration() {
        // 限流配置
        assertThat(properties.getRateLimit().isEnabled()).isFalse();
        assertThat(properties.getRateLimit().getImplementation()).isEqualTo("redisson");
        assertThat(properties.getRateLimit().getImplementationType().isRedisson()).isTrue();

        // 防重复提交配置
        assertThat(properties.getRepeatSubmit().isEnabled()).isFalse();
        assertThat(properties.getRepeatSubmit().getImplementation()).isEqualTo("redis-template");
        assertThat(properties.getRepeatSubmit().getImplementationType().isRedisTemplate()).isTrue();

        // Redisson 开关
        assertThat(properties.getRedisson().isEnabled()).isTrue();
    }
}


