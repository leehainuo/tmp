package com.miku.core.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置
 * <p>
 * 使用条件注解，使 Redis 成为可选依赖
 * 如果 Redis 不可用，应用仍可正常启动（使用本地缓存降级）
 *
 * @author lihainuo.com
 */
@Slf4j
@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    /**
     * RedisTemplate 配置
     * <p>
     * 注意：这里不再使用 @ConditionalOnBean(RedisConnectionFactory.class)。
     * 否则在 Spring Boot 自动配置 RedisConnectionFactory 之前，本配置类就会被解析，
     * 导致条件判断为 false，RedisTemplate 根本不会被注册，从而让上层代码误判「Redis 不可用」。
     * <p>
     * 依赖注入会自动保证在创建 RedisTemplate 之前已经有 RedisConnectionFactory Bean。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("[Miku-Redis] 初始化 RedisTemplate");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(serializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}

