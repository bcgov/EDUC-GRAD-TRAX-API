package ca.bc.gov.educ.api.trax.config;

import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableRedisRepositories("ca.bc.gov.educ.api.trax.repository.redis")
public class RedisConfig {
    @Autowired
    private EducGradTraxApiConstants constants;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        redisClusterConfiguration.addClusterNode(new RedisClusterNode(
                constants.getRedisUrl(),
                Integer.parseInt(constants.getRedisPort())));
        redisClusterConfiguration.setPassword(constants.getRedisSecret());
        return new JedisConnectionFactory(redisClusterConfiguration);
    }

    @Bean
    public JedisCluster jedisCluster() {
        Set<HostAndPort> jedisClusterNodes = new HashSet<>();
        jedisClusterNodes.add(
                new HostAndPort(constants.getRedisUrl(),
                        Integer.parseInt(constants.getRedisPort())));

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(0);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWait(Duration.ofSeconds(1));
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(1));

        return new JedisCluster(jedisClusterNodes, 5000, 5000, 3,
                constants.getRedisUser(), constants.getRedisSecret(),
                "educ-grad-trax-api", poolConfig);
    }
}
