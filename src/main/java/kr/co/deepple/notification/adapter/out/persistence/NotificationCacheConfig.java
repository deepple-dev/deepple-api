package kr.co.deepple.notification.adapter.out.persistence;

import java.time.Duration;
import kr.co.deepple.notification.application.NotificationDataReader;
import kr.co.deepple.notification.application.dto.NotificationTemplateInfo;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

@Configuration
public class NotificationCacheConfig {

  private static final Duration TEMPLATE_TTL = Duration.ofHours(1);

  @Bean
  public RedisCacheManagerBuilderCustomizer notificationCacheCustomizer() {
    return builder ->
        builder.withCacheConfiguration(
            NotificationDataReader.TEMPLATE_CACHE,
            builder
                .cacheDefaults()
                .entryTtl(TEMPLATE_TTL)
                .serializeValuesWith(
                    SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(NotificationTemplateInfo.class))));
  }
}
