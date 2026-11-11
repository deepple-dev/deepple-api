package deepple.deepple.common.config;

import org.redisson.config.BaseConfig;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.ReflectionUtils;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.lang.reflect.Method;
import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    private static final int REDIS_RETRY_ATTEMPTS = 1;

    @Bean
    public RedissonAutoConfigurationCustomizer redissonRetryCustomizer() {
        return config -> serverConfig(config).setRetryAttempts(REDIS_RETRY_ATTEMPTS);
    }

    private static BaseConfig<?> serverConfig(Config config) {
        String getterName;
        if (config.isClusterConfig()) {
            getterName = "getClusterServersConfig";
        } else if (config.isSingleConfig()) {
            getterName = "getSingleServerConfig";
        } else {
            throw new IllegalStateException("지원하지 않는 Redis 서버 구성입니다.");
        }
        Method getter = ReflectionUtils.findMethod(Config.class, getterName);
        ReflectionUtils.makeAccessible(getter);
        return (BaseConfig<?>) ReflectionUtils.invokeMethod(getter, config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(jsonRedisSerializer())
            )
            .entryTtl(Duration.ofDays(30))
            .prefixCacheNameWith("cache::");

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withCacheConfiguration("notificationTemplate", config.entryTtl(Duration.ofHours(1)))
            .build();
    }

    // 값에 타입 정보를 함께 저장해 역직렬화하므로, 역직렬화할 수 있는 타입을 앱 클래스와 JDK 기본 타입으로 제한한다
    static GenericJacksonJsonRedisSerializer jsonRedisSerializer() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("deepple.deepple.")
            .allowIfSubType("java.lang.")
            .allowIfSubType("java.util.")
            .allowIfSubType("java.time.")
            .build();
        return GenericJacksonJsonRedisSerializer.builder()
            .enableDefaultTyping(typeValidator)
            .build();
    }
}
