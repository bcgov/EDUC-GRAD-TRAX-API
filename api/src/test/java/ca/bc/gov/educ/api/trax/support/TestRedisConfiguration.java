package ca.bc.gov.educ.api.trax.support;

import java.io.File;

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
    // Allow a native Redis binary on platforms unsupported by embedded-redis.
    String executable = System.getProperty("embedded.redis.executable");
    this.redisServer = executable == null
        ? RedisServer.builder().port(6370).build()
        : new RedisServer(new File(executable), 6370);
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
