package kr.co.deepple.feed.adapter.out.persistence;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import kr.co.deepple.feed.application.port.out.FeedVoteCountCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedVoteCountCacheRepositoryImpl implements FeedVoteCountCacheRepository {

  private static final String KEY_PREFIX = "feed:vote-count:v1:";
  private static final String CHANGED_KEY = "feed:vote-count:changed:v1";
  private static final Duration TTL = Duration.ofDays(7);
  private static final RedisScript<Long> INCREMENT_IF_PRESENT =
      RedisScript.of(
          """
          if redis.call('EXISTS', KEYS[1]) == 0 then
            return nil
          end
          local voteCount = redis.call('HINCRBY', KEYS[1], ARGV[1], 1)
          redis.call('EXPIRE', KEYS[1], ARGV[2])
          return voteCount
          """,
          Long.class);
  private static final RedisScript<Long> SAVE_IF_ABSENT =
      RedisScript.of(
          """
          if redis.call('EXISTS', KEYS[1]) == 1 then
            return 0
          end
          redis.call('HSET', KEYS[1], unpack(ARGV, 2))
          redis.call('EXPIRE', KEYS[1], ARGV[1])
          return 1
          """,
          Long.class);
  private static final RedisScript<Long> UPDATE_IF_PRESENT =
      RedisScript.of(
          """
          if redis.call('EXISTS', KEYS[1]) == 0 then
            return 0
          end
          redis.call('HSET', KEYS[1], unpack(ARGV))
          return 1
          """,
          Long.class);

  private final StringRedisTemplate redisTemplate;

  @Override
  public Map<Long, Map<Long, Long>> findAll(Collection<Long> feedIds) {
    if (feedIds.isEmpty()) {
      return Map.of();
    }
    List<Long> ids = List.copyOf(feedIds);
    List<Object> hashes =
        redisTemplate.executePipelined(
            (RedisCallback<Object>)
                connection -> {
                  ids.forEach(
                      feedId -> connection.hashCommands().hGetAll(toBytes(buildKey(feedId))));
                  return null;
                });
    Map<Long, Map<Long, Long>> voteCounts = new HashMap<>();
    for (int i = 0; i < ids.size(); i++) {
      Map<?, ?> hash = (Map<?, ?>) hashes.get(i);
      if (!hash.isEmpty()) {
        voteCounts.put(ids.get(i), toVoteCounts(hash));
      }
    }
    return voteCounts;
  }

  @Override
  public void saveAllIfAbsent(Map<Long, Map<Long, Long>> voteCounts) {
    voteCounts.forEach(this::saveIfAbsent);
  }

  @Override
  public void saveChangedIfAbsent(long feedId, Map<Long, Long> voteCounts) {
    saveIfAbsent(feedId, voteCounts);
    markChanged(feedId);
  }

  @Override
  public boolean incrementIfPresent(long feedId, long optionId) {
    Long voteCount =
        redisTemplate.execute(
            INCREMENT_IF_PRESENT,
            List.of(buildKey(feedId)),
            String.valueOf(optionId),
            String.valueOf(TTL.toSeconds()));
    if (voteCount == null) {
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
  public void updateIfPresent(long feedId, Map<Long, Long> voteCounts) {
    redisTemplate.execute(
        UPDATE_IF_PRESENT, List.of(buildKey(feedId)), toFieldValues(voteCounts).toArray());
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

  private Map<Long, Long> toVoteCounts(Map<?, ?> hash) {
    Map<Long, Long> voteCounts = new HashMap<>();
    hash.forEach((optionId, voteCount) ->
        voteCounts.put(Long.parseLong((String) optionId), Long.parseLong((String) voteCount)));
    return voteCounts;
  }

  private void saveIfAbsent(long feedId, Map<Long, Long> voteCounts) {
    Object[] args =
        Stream.concat(
                Stream.of(String.valueOf(TTL.toSeconds())), toFieldValues(voteCounts).stream())
            .toArray();
    redisTemplate.execute(SAVE_IF_ABSENT, List.of(buildKey(feedId)), args);
  }

  private List<String> toFieldValues(Map<Long, Long> voteCounts) {
    return voteCounts.entrySet().stream()
        .flatMap(
            entry -> Stream.of(String.valueOf(entry.getKey()), String.valueOf(entry.getValue())))
        .toList();
  }

  private void markChanged(long feedId) {
    redisTemplate.opsForSet().add(CHANGED_KEY, String.valueOf(feedId));
  }
}
