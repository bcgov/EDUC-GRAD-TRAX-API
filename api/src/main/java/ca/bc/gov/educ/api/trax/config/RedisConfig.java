package ca.bc.gov.educ.api.trax.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Component;

@Component
@EnableRedisRepositories(basePackages = "ca.bc.gov.educ.api.trax.repository.redis")
public class RedisConfig {

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisConfig(final RedisConnectionFactory redisConnectionFactory) {
        this.stringRedisTemplate = new StringRedisTemplate(redisConnectionFactory);
    }

    public StringRedisTemplate getStringRedisTemplate() {
        return stringRedisTemplate;
    }
}
