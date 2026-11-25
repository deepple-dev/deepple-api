package kr.co.deepple.feed.adapter.out.persistence;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import kr.co.deepple.feed.application.port.out.VotedFeedCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VotedFeedCacheRepositoryImpl implements VotedFeedCacheRepository {

  private static final String KEY_PREFIX = "feed:voted:v1:";
  private static final String LOADED_MARKER = "loaded";
  private static final String LOADED_VALUE = "1";
  private static final Duration TTL = Duration.ofHours(1);
  private static final RedisScript<Long> ADD_IF_PRESENT =
      RedisScript.of(
          """
          if redis.call('EXISTS', KEYS[1]) == 1 then
            return redis.call('HSET', KEYS[1], ARGV[1], ARGV[2])
          end
          return nil
          """,
          Long.class);
  private static final RedisScript<Long> SAVE_IF_ABSENT =
      RedisScript.of(
          """
          if redis.call('EXISTS', KEYS[1]) == 1 then
            return 0
          end
          for i = 2, #ARGV, 1000 do
            redis.call('HSET', KEYS[1], unpack(ARGV, i, math.min(i + 999, #ARGV)))
          end
          redis.call('EXPIRE', KEYS[1], ARGV[1])
          return 1
          """,
          Long.class);

  private final StringRedisTemplate redisTemplate;

  @Override
  public Optional<Map<Long, Long>> findVotedOptionIds(long memberId, Set<Long> feedIds) {
    List<Long> ids = List.copyOf(feedIds);
    List<Object> fields = new ArrayList<>();
    fields.add(LOADED_MARKER);
    ids.forEach(feedId -> fields.add(String.valueOf(feedId)));
    List<Object> values = redisTemplate.opsForHash().multiGet(buildKey(memberId), fields);
    if (!isLoaded(values)) {
      return Optional.empty();
    }
    Map<Long, Long> votedOptionIds = new HashMap<>();
    for (int i = 0; i < ids.size(); i++) {
      Object optionId = values.get(i + 1);
      if (optionId != null) {
        votedOptionIds.put(ids.get(i), Long.parseLong((String) optionId));
      }
    }
    return Optional.of(votedOptionIds);
  }

  @Override
  public boolean exists(long memberId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(memberId)));
  }

  @Override
  public void saveIfAbsent(long memberId, Map<Long, Long> votedOptionIds) {
    Object[] args =
        Stream.concat(
                Stream.of(String.valueOf(TTL.toSeconds()), LOADED_MARKER, LOADED_VALUE),
                votedOptionIds.entrySet().stream()
                    .flatMap(entry ->
                        Stream.of(
                            String.valueOf(entry.getKey()), String.valueOf(entry.getValue()))))
            .toArray();
    redisTemplate.execute(SAVE_IF_ABSENT, List.of(buildKey(memberId)), args);
  }

  @Override
  public void addIfPresent(long memberId, long feedId, long optionId) {
    redisTemplate.execute(
        ADD_IF_PRESENT,
        List.of(buildKey(memberId)),
        String.valueOf(feedId),
        String.valueOf(optionId));
  }

  private boolean isLoaded(List<Object> values) {
    return values != null && LOADED_VALUE.equals(values.getFirst());
  }

  private String buildKey(long memberId) {
    return KEY_PREFIX + memberId;
  }
}
