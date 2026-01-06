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

  private static RedisServer redisServer;
  private static int referenceCount = 0;
  private static final Object lock = new Object();

  @PostConstruct
  public void postConstruct() {
    synchronized (lock) {
      if (redisServer == null) {
        redisServer = RedisServer.builder().port(6370).build();
        redisServer.start();
      }
      referenceCount++;
    }
  }

  @PreDestroy
  public void preDestroy() {
    synchronized (lock) {
      referenceCount--;
      if (referenceCount <= 0 && redisServer != null) {
        redisServer.stop();
        redisServer = null;
        referenceCount = 0;
      }
    }
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
