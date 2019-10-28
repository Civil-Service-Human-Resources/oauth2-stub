package uk.gov.cshr.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableAutoConfiguration
public class SessionConfig {

    @Bean(name = "redisTemplate")
    public StringRedisTemplate getDefaultRedisTemplate() {

        final StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory());

        return redisTemplate;
    }

    @Bean
    public LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory();
    }
}
