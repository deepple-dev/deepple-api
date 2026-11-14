package kr.co.deepple.recommendation.adapter.out.persistence;

import java.util.Date;
import java.util.Set;
import kr.co.deepple.recommendation.application.port.out.IntroductionCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class IntroductionCacheRepositoryImpl implements IntroductionCacheRepository {

  private static final String PREFIX = "introduction:";

  private final ObjectMapper objectMapper;
  private final RedisTemplate<String, String> redisTemplate;

  @Override
  public void saveIntroductionMemberIds(
      String key, Set<Long> introductionMemberIds, Date expireAt) {
    String fullKey = buildKey(key);
    String json = objectMapper.writeValueAsString(introductionMemberIds);
    redisTemplate.opsForValue().set(fullKey, json);
    redisTemplate.expireAt(fullKey, expireAt);
  }

  @Override
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
