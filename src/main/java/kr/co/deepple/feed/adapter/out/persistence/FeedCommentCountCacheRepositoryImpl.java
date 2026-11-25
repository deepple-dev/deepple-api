package kr.co.deepple.feed.adapter.out.persistence;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.co.deepple.feed.application.port.out.FeedCommentCountCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.SetCondition;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedCommentCountCacheRepositoryImpl implements FeedCommentCountCacheRepository {

  private static final String KEY_PREFIX = "feed:comment-count:v1:";
  private static final String CHANGED_KEY = "feed:comment-count:changed:v1";
  private static final Duration TTL = Duration.ofDays(7);
  private static final RedisScript<Long> INCREMENT_IF_PRESENT =
      RedisScript.of(
          """
          if redis.call('EXISTS', KEYS[1]) == 0 then
            return nil
          end
          local commentCount = redis.call('INCR', KEYS[1])
          redis.call('EXPIRE', KEYS[1], ARGV[1])
          return commentCount
          """,
          Long.class);
  private static final RedisScript<Long> UPDATE_IF_UNCHANGED =
      RedisScript.of(
          """
          if redis.call('GET', KEYS[1]) ~= ARGV[1] then
            return 0
          end
          redis.call('SET', KEYS[1], ARGV[2], 'KEEPTTL')
          return 1
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
    Map<Long, Long> commentCounts = new HashMap<>();
    for (int i = 0; i < ids.size(); i++) {
      String value = values.get(i);
      if (value != null) {
        commentCounts.put(ids.get(i), Long.parseLong(value));
      }
    }
    return commentCounts;
  }

  @Override
  public void saveAllIfAbsent(Map<Long, Long> commentCounts) {
    if (commentCounts.isEmpty()) {
      return;
    }
    redisTemplate.executePipelined(
        (RedisCallback<Object>)
            connection -> {
              commentCounts.forEach((feedId, commentCount) ->
                  connection
                      .stringCommands()
                      .set(
                          toBytes(buildKey(feedId)),
                          toBytes(String.valueOf(commentCount)),
                          SetCondition.ifAbsent(),
                          Expiration.from(TTL)));
              return null;
            });
  }

  @Override
  public boolean exists(long feedId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(feedId)));
  }

  @Override
  public void saveChangedIfAbsent(long feedId, long commentCount) {
    redisTemplate.opsForValue().setIfAbsent(buildKey(feedId), String.valueOf(commentCount), TTL);
    markChanged(feedId);
  }

  @Override
  public boolean incrementIfPresent(long feedId) {
    Long commentCount =
        redisTemplate.execute(
            INCREMENT_IF_PRESENT, List.of(buildKey(feedId)), String.valueOf(TTL.toSeconds()));
    if (commentCount == null) {
      return false;
    }
    markChanged(feedId);
    return true;
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
  public void updateIfUnchanged(long feedId, long expectedCount, long commentCount) {
    redisTemplate.execute(
        UPDATE_IF_UNCHANGED,
        List.of(buildKey(feedId)),
        String.valueOf(expectedCount),
        String.valueOf(commentCount));
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

  private void markChanged(long feedId) {
    redisTemplate.opsForSet().add(CHANGED_KEY, String.valueOf(feedId));
  }
}
