package deepple.deepple.member.command.infra.member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class AuthMessageRedisRepository {
    private static final String PREFIX = "authMessage:";
    private static final Duration EXPIRE_TIME = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;

    public void save(String key, String message) {
        String fullKey = getFullKey(key);
        redisTemplate.opsForValue().set(fullKey, message, EXPIRE_TIME);
    }

    public String getByKey(String key) {
        String fullKey = getFullKey(key);
        return redisTemplate.opsForValue().get(fullKey);
    }

    public void delete(String key) {
        String fullKey = getFullKey(key);
        redisTemplate.delete(fullKey);
    }

    private String getFullKey(String key) {
        return PREFIX + key;
    }
}
