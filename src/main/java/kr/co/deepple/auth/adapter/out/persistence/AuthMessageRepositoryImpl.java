package kr.co.deepple.auth.adapter.out.persistence;

import java.time.Duration;
import kr.co.deepple.auth.application.port.out.AuthMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthMessageRepositoryImpl implements AuthMessageRepository {

  private static final String PREFIX = "authMessage:";
  private static final Duration EXPIRE_TIME = Duration.ofMinutes(5);

  private final RedisTemplate<String, String> redisTemplate;

  @Override
  public void save(String key, String message) {
    String fullKey = getFullKey(key);
    redisTemplate.opsForValue().set(fullKey, message, EXPIRE_TIME);
  }

  @Override
  public String getByKey(String key) {
    String fullKey = getFullKey(key);
    return redisTemplate.opsForValue().get(fullKey);
  }

  @Override
  public void delete(String key) {
    String fullKey = getFullKey(key);
    redisTemplate.delete(fullKey);
  }

  private String getFullKey(String key) {
    return PREFIX + key;
  }
}
