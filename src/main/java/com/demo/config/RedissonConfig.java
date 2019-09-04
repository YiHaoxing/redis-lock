package com.demo.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;

/**
 * @author YiHaoXing
 * @version 1.0.0
 * @className com.demo.config.RedissonConfig
 * @description TODO
 * @date 2019/7/11 0:03
 */
@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redisson() throws IOException {
        //Config config = Config.fromYAML(new ClassPathResource("redisson-cluster.yml").getInputStream());
        Config config = Config.fromYAML(new ClassPathResource("redisson-single.yml").getInputStream());
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

    /*@Bean
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redissonClient) {
        return new RedissonConnectionFactory(redissonClient);
    }*/

    /*@Bean("redisTemplate")
    public RedisTemplate getRedisTemplate(RedisConnectionFactory redissonConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redissonConnectionFactory);
        redisTemplate.setValueSerializer(valueSerializer());
        redisTemplate.setKeySerializer(keySerializer());
        redisTemplate.setHashKeySerializer(keySerializer());
        redisTemplate.setHashValueSerializer(valueSerializer());
        return redisTemplate;
    }*/

    @Bean
    public RedisSerializer keySerializer() {
        return new StringRedisSerializer();
    }

    @Bean
    public RedisSerializer valueSerializer() {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        return jackson2JsonRedisSerializer;
    }
}
