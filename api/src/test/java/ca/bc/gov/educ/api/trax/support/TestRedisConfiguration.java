package ca.bc.gov.educ.api.trax.support;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

@TestConfiguration
@Profile("redisTest")
public class TestRedisConfiguration {

  private final RedisServer redisServer;

  public TestRedisConfiguration() {
    this.redisServer = RedisServer.builder().setting("maxmemory 128M").port(6370).build();
  }

  @PostConstruct
  public void postConstruct() {
    this.redisServer.start();
  }

  @PreDestroy
  public void preDestroy() {
    this.redisServer.stop();
  }
}

