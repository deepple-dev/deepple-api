package kr.co.deepple.datingexam.adapter.database;

import kr.co.deepple.datingexam.application.dto.DatingExamInfoResponse;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

@Configuration
public class DatingExamCacheConfig {

    private static final Duration DATING_EXAM_INFO_TTL = Duration.ofHours(1);

    @Bean
    public RedisCacheManagerBuilderCustomizer datingExamCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
            DatingExamQueryRepositoryImpl.DATING_EXAM_INFO_CACHE,
            builder.cacheDefaults()
                .entryTtl(DATING_EXAM_INFO_TTL)
                .serializeValuesWith(SerializationPair.fromSerializer(
                    new JacksonJsonRedisSerializer<>(DatingExamInfoResponse.class)))
        );
    }
}
