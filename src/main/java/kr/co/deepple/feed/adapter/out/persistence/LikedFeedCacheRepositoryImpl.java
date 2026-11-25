package kr.co.deepple.feed.adapter.out.persistence;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kr.co.deepple.feed.application.port.out.LikedFeedCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LikedFeedCacheRepositoryImpl implements LikedFeedCacheRepository {

  private static final String KEY_PREFIX = "feed:liked:v1:";
  private static final String LOADED_MARKER = "loaded";
  private static final Duration TTL = Duration.ofHours(1);
  private static final RedisScript<Long> ADD_IF_PRESENT =
      RedisScript.of(
          """
          if redis.call('EXISTS', KEYS[1]) == 1 then
            return redis.call('SADD', KEYS[1], ARGV[1])
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
            redis.call('SADD', KEYS[1], unpack(ARGV, i, math.min(i + 999, #ARGV)))
          end
          redis.call('EXPIRE', KEYS[1], ARGV[1])
          return 1
          """,
          Long.class);

  private final StringRedisTemplate redisTemplate;

  @Override
  public Optional<Set<Long>> findLikedFeedIds(long memberId, Set<Long> feedIds) {
    List<Object> members = new ArrayList<>();
    members.add(LOADED_MARKER);
    feedIds.forEach(feedId -> members.add(String.valueOf(feedId)));
    Map<Object, Boolean> isMembers =
        redisTemplate.opsForSet().isMember(buildKey(memberId), members.toArray());
    if (!isLoaded(isMembers)) {
      return Optional.empty();
    }
    return Optional.of(
        feedIds.stream()
            .filter(feedId -> Boolean.TRUE.equals(isMembers.get(String.valueOf(feedId))))
            .collect(Collectors.toSet()));
  }

  @Override
  public boolean exists(long memberId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(memberId)));
  }

  @Override
  public void saveIfAbsent(long memberId, Collection<Long> likedFeedIds) {
    Object[] args =
        Stream.concat(
                Stream.of(String.valueOf(TTL.toSeconds()), LOADED_MARKER),
                likedFeedIds.stream().map(String::valueOf))
            .toArray();
    redisTemplate.execute(SAVE_IF_ABSENT, List.of(buildKey(memberId)), args);
  }

  @Override
  public void addIfPresent(long memberId, long feedId) {
    redisTemplate.execute(ADD_IF_PRESENT, List.of(buildKey(memberId)), String.valueOf(feedId));
  }

  @Override
  public void remove(long memberId, long feedId) {
    redisTemplate.opsForSet().remove(buildKey(memberId), String.valueOf(feedId));
  }

  private boolean isLoaded(Map<Object, Boolean> isMembers) {
    return isMembers != null && Boolean.TRUE.equals(isMembers.get(LOADED_MARKER));
  }

  private String buildKey(long memberId) {
    return KEY_PREFIX + memberId;
  }
}
