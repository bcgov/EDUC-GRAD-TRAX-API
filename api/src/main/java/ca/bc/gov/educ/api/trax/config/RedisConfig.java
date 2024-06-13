package ca.bc.gov.educ.api.trax.config;

import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories("ca.bc.gov.educ.api.trax.repository.redis")
public class RedisConfig {
    @Autowired
    private EducGradTraxApiConstants constants;
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(constants.getRedisUrl());
        redisStandaloneConfiguration.setPort(Integer.parseInt(constants.getRedisPort()));
        redisStandaloneConfiguration.setPassword(constants.getRedisSecret());

        /*//Cluster Configuration
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        RedisNode node0 = new RedisNode(constants.getRedisUrl(), Integer.parseInt(constants.getRedisPort()));
        redisClusterConfiguration.addClusterNode(node0);*/

        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        template.setHashKeySerializer(new JdkSerializationRedisSerializer());
        template.setHashValueSerializer(new JdkSerializationRedisSerializer());
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();

        return template;
    }
}
