package kr.co.deepple.feed.adapter.out.persistence;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.dto.FeedAuthorSnapshot;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.port.out.FeedSnapshotCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.SetCondition;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class FeedSnapshotCacheRepositoryImpl implements FeedSnapshotCacheRepository {

  private static final String FEED_KEY_PREFIX = "feed:content:v1:";
  private static final String AUTHOR_KEY_PREFIX = "feed:author:v1:";
  private static final Duration TTL = Duration.ofHours(1);
  private static final String INVALIDATED_MARKER = "invalidated";
  private static final Duration INVALIDATED_TTL = Duration.ofSeconds(10);

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public List<FeedSnapshot> findFeeds(Collection<Long> feedIds) {
    return findAll(FEED_KEY_PREFIX, feedIds, FeedSnapshot.class);
  }

  @Override
  public void saveFeeds(Collection<FeedSnapshot> feeds) {
    saveAll(FEED_KEY_PREFIX, feeds, FeedSnapshot::feedId);
  }

  @Override
  public void invalidateFeed(long feedId) {
    invalidate(FEED_KEY_PREFIX + feedId);
  }

  @Override
  public List<FeedAuthorSnapshot> findAuthors(Collection<Long> authorIds) {
    return findAll(AUTHOR_KEY_PREFIX, authorIds, FeedAuthorSnapshot.class);
  }

  @Override
  public void saveAuthors(Collection<FeedAuthorSnapshot> authors) {
    saveAll(AUTHOR_KEY_PREFIX, authors, FeedAuthorSnapshot::authorId);
  }

  @Override
  public void invalidateAuthor(long authorId) {
    invalidate(AUTHOR_KEY_PREFIX + authorId);
  }

  private void invalidate(String key) {
    redisTemplate.opsForValue().set(key, INVALIDATED_MARKER, INVALIDATED_TTL);
  }

  private <T> List<T> findAll(String keyPrefix, Collection<Long> ids, Class<T> type) {
    if (ids.isEmpty()) {
      return List.of();
    }
    List<String> values =
        redisTemplate.opsForValue().multiGet(ids.stream().map(id -> keyPrefix + id).toList());
    if (values == null) {
      return List.of();
    }
    return values.stream()
        .filter(value -> value != null && !INVALIDATED_MARKER.equals(value))
        .map(value -> objectMapper.readValue(value, type))
        .toList();
  }

  private <T> void saveAll(String keyPrefix, Collection<T> values, Function<T, Long> idGetter) {
    if (values.isEmpty()) {
      return;
    }
    Map<String, String> jsonByKey =
        values.stream()
            .collect(
                Collectors.toMap(
                    value -> keyPrefix + idGetter.apply(value), objectMapper::writeValueAsString));
    redisTemplate.executePipelined(
        (RedisCallback<Object>)
            connection -> {
              jsonByKey.forEach((key, json) ->
                  connection
                      .stringCommands()
                      .set(
                          toBytes(key),
                          toBytes(json),
                          SetCondition.ifAbsent(),
                          Expiration.from(TTL)));
              return null;
            });
  }

  private byte[] toBytes(String value) {
    return value.getBytes(StandardCharsets.UTF_8);
  }
}
