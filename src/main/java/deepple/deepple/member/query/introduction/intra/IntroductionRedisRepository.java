package deepple.deepple.member.query.introduction.intra;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class IntroductionRedisRepository {
    private static final String PREFIX = "introduction:";

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public void saveIntroductionMemberIds(String key, Set<Long> introductionMemberIds, Date expireAt) {
        String fullKey = buildKey(key);
        String json = objectMapper.writeValueAsString(introductionMemberIds);
        redisTemplate.opsForValue().set(fullKey, json);
        redisTemplate.expireAt(fullKey, expireAt);
    }

    public Set<Long> findIntroductionMemberIds(String key) {
        String fullKey = buildKey(key);
        String json = redisTemplate.opsForValue().get(fullKey);
        if (json == null) {
            return Set.of();
        }
        return objectMapper.readValue(json, new TypeReference<Set<Long>>() {});
    }

    private String buildKey(String key) {
        return PREFIX + key;
    }
}
