package ca.bc.gov.educ.api.trax.support;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisCluster;
import redis.embedded.RedisServer;

@TestConfiguration
@Profile("redisTest")
public class TestRedisConfiguration {

  private final RedisServer redisServer;

  public TestRedisConfiguration() {
    this.redisServer = RedisServer.builder().port(6370).build();
  }

  @PostConstruct
  public void postConstruct() {
    this.redisServer.start();
  }

  @PreDestroy
  public void preDestroy() {
    this.redisServer.stop();
  }

  @Bean(name = "testJedisConnectionFactory")
  public JedisConnectionFactory jedisConnectionFactory() {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration("localhost", 6370);
    return new JedisConnectionFactory(redisStandaloneConfiguration);
  }

  @Bean
  public JedisCluster jedisCluster() {
    return Mockito.mock(JedisCluster.class);
  }
}
