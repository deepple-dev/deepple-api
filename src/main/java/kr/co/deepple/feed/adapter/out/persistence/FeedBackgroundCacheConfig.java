package kr.co.deepple.feed.adapter.out.persistence;

import java.time.Duration;
import kr.co.deepple.feed.application.FeedBackgroundReader;
import kr.co.deepple.feed.application.dto.FeedBackgroundCatalog;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

@Configuration
public class FeedBackgroundCacheConfig {

  private static final Duration CATALOG_TTL = Duration.ofHours(1);

  @Bean
  public RedisCacheManagerBuilderCustomizer feedBackgroundCacheCustomizer() {
    return builder ->
        builder.withCacheConfiguration(
            FeedBackgroundReader.CATALOG_CACHE,
            builder
                .cacheDefaults()
                .entryTtl(CATALOG_TTL)
                .serializeValuesWith(
                    SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(FeedBackgroundCatalog.class))));
  }
}
