package kr.co.deepple.feed.adapter.out.persistence;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.SetCondition;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedLikeCountCacheRepositoryImpl implements FeedLikeCountCacheRepository {

  private static final String KEY_PREFIX = "feed:like-count:v1:";
  private static final String CHANGED_KEY = "feed:like-count:changed:v1";
  private static final Duration TTL = Duration.ofDays(7);
  private static final RedisScript<Long> INCREMENT_IF_PRESENT =
      RedisScript.of(
          """
          if redis.call('EXISTS', KEYS[1]) == 0 then
            return nil
          end
          local likeCount = redis.call('INCRBY', KEYS[1], ARGV[1])
          redis.call('EXPIRE', KEYS[1], ARGV[2])
          return likeCount
          """,
          Long.class);

  private final StringRedisTemplate redisTemplate;

  @Override
  public Map<Long, Long> findAll(Collection<Long> feedIds) {
    if (feedIds.isEmpty()) {
      return Map.of();
    }
    List<Long> ids = List.copyOf(feedIds);
    List<String> values =
        redisTemplate.opsForValue().multiGet(ids.stream().map(this::buildKey).toList());
    if (values == null) {
      return Map.of();
    }
    Map<Long, Long> likeCounts = new HashMap<>();
    for (int i = 0; i < ids.size(); i++) {
      String value = values.get(i);
      if (value != null) {
        likeCounts.put(ids.get(i), Long.parseLong(value));
      }
    }
    return likeCounts;
  }

  @Override
  public void saveAllIfAbsent(Map<Long, Long> likeCounts) {
    if (likeCounts.isEmpty()) {
      return;
    }
    redisTemplate.executePipelined(
        (RedisCallback<Object>)
            connection -> {
              likeCounts.forEach((feedId, likeCount) ->
                  connection
                      .stringCommands()
                      .set(
                          toBytes(buildKey(feedId)),
                          toBytes(String.valueOf(likeCount)),
                          SetCondition.ifAbsent(),
                          Expiration.from(TTL)));
              return null;
            });
  }

  @Override
  public void saveChangedIfAbsent(long feedId, long likeCount) {
    redisTemplate.opsForValue().setIfAbsent(buildKey(feedId), String.valueOf(likeCount), TTL);
    markChanged(feedId);
  }

  @Override
  public boolean incrementIfPresent(long feedId) {
    return addIfPresent(feedId, 1L);
  }

  @Override
  public boolean decrementIfPresent(long feedId) {
    return addIfPresent(feedId, -1L);
  }

  @Override
  public List<Long> popChangedFeedIds(int count) {
    List<String> feedIds = redisTemplate.opsForSet().pop(CHANGED_KEY, count);
    if (feedIds == null) {
      return List.of();
    }
    return feedIds.stream().map(Long::parseLong).toList();
  }

  @Override
  public void updateIfPresent(long feedId, long likeCount) {
    redisTemplate.execute(
        (RedisCallback<Boolean>)
            connection ->
                connection
                    .stringCommands()
                    .set(
                        toBytes(buildKey(feedId)),
                        toBytes(String.valueOf(likeCount)),
                        SetCondition.ifPresent(),
                        Expiration.keepTtl()));
  }

  @Override
  public void delete(long feedId) {
    redisTemplate.delete(buildKey(feedId));
  }

  private String buildKey(long feedId) {
    return KEY_PREFIX + feedId;
  }

  private byte[] toBytes(String value) {
    return value.getBytes(StandardCharsets.UTF_8);
  }

  private boolean addIfPresent(long feedId, long delta) {
    Long likeCount =
        redisTemplate.execute(
            INCREMENT_IF_PRESENT,
            List.of(buildKey(feedId)),
            String.valueOf(delta),
            String.valueOf(TTL.toSeconds()));
    if (likeCount == null) {
      return false;
    }
    markChanged(feedId);
    return true;
  }

  private void markChanged(long feedId) {
    redisTemplate.opsForSet().add(CHANGED_KEY, String.valueOf(feedId));
  }
}
