package kr.co.deepple.auth.infra;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.domain.UsedRefreshToken;
import kr.co.deepple.common.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

  private static final String ACTIVE_KEY = "refresh:active:%s";
  private static final String USED_KEY = "refresh:used:%s";
  private static final String OWNER_KEY = "refresh:owner:%s:%d";
  private static final String MEMBER_GROUP = "MEMBER";
  private static final String ADMIN_GROUP = "ADMIN";
  private static final String DELIMITER = ":";

  private final StringRedisTemplate redisTemplate;

  public void save(String tokenHash, TokenOwner owner, Duration lifetime) {
    String ownerKey = ownerKey(owner);
    redisTemplate.opsForValue().set(activeKey(tokenHash), serialize(owner), lifetime);
    redisTemplate.opsForSet().add(ownerKey, tokenHash);
    redisTemplate.expire(ownerKey, lifetime);
  }

  public Optional<TokenOwner> findOwner(String tokenHash) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(activeKey(tokenHash)))
        .map(RefreshTokenRepository::deserialize);
  }

  public boolean markUsed(String tokenHash, TokenOwner owner, Instant usedAt, Duration lifetime) {
    String value = serialize(owner) + DELIMITER + usedAt.toEpochMilli();
    return Boolean.TRUE.equals(
        redisTemplate.opsForValue().setIfAbsent(usedKey(tokenHash), value, lifetime));
  }

  public Optional<UsedRefreshToken> findUsed(String tokenHash) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(usedKey(tokenHash)))
        .map(value -> {
          int lastDelimiter = value.lastIndexOf(DELIMITER);
          TokenOwner owner = deserialize(value.substring(0, lastDelimiter));
          Instant usedAt = Instant.ofEpochMilli(Long.parseLong(value.substring(lastDelimiter + 1)));
          return new UsedRefreshToken(owner, usedAt);
        });
  }

  public void delete(String tokenHash, TokenOwner owner) {
    redisTemplate.delete(activeKey(tokenHash));
    redisTemplate.opsForSet().remove(ownerKey(owner), tokenHash);
  }

  public void deleteAll(TokenOwner owner) {
    String ownerKey = ownerKey(owner);
    Set<String> tokenHashes = redisTemplate.opsForSet().members(ownerKey);
    if (tokenHashes != null && !tokenHashes.isEmpty()) {
      redisTemplate.delete(tokenHashes.stream().map(RefreshTokenRepository::activeKey).toList());
    }
    redisTemplate.delete(ownerKey);
  }

  private static String activeKey(String tokenHash) {
    return ACTIVE_KEY.formatted(tokenHash);
  }

  private static String usedKey(String tokenHash) {
    return USED_KEY.formatted(tokenHash);
  }

  private static String ownerKey(TokenOwner owner) {
    return OWNER_KEY.formatted(owner.isMember() ? MEMBER_GROUP : ADMIN_GROUP, owner.id());
  }

  private static String serialize(TokenOwner owner) {
    return owner.role().name() + DELIMITER + owner.id();
  }

  private static TokenOwner deserialize(String value) {
    String[] parts = value.split(DELIMITER);
    return new TokenOwner(Long.parseLong(parts[1]), Role.valueOf(parts[0]));
  }
}
