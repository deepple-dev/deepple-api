package kr.co.deepple.feed.adapter.out.persistence;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.dto.HomeFeedSortKey;
import kr.co.deepple.feed.application.port.out.HomeFeedCacheRepository;
import kr.co.deepple.feed.domain.FeedGrades;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HomeFeedCacheRepositoryImpl implements HomeFeedCacheRepository {

  private static final String KEY_PREFIX = "feed:home:v1:";
  private static final String LOADING_KEY_INFIX = ":loading:";
  private static final String LOADED_MARKER = "loaded";
  private static final double LOADED_MARKER_SCORE = -1.0;
  private static final double MIN_FEED_SCORE = 0.0;
  private static final String MEMBER_FORMAT = "%019d";
  private static final int ADD_BATCH_SIZE = 1000;
  private static final Duration TTL = Duration.ofHours(1);

  private final StringRedisTemplate redisTemplate;

  @Override
  public boolean isLoaded(String grade) {
    return redisTemplate.opsForZSet().score(buildKey(grade), LOADED_MARKER) != null;
  }

  @Override
  public List<HomeFeedSortKey> findSortKeys(String grade, long maxScore, int count) {
    Set<TypedTuple<String>> tuples =
        redisTemplate
            .opsForZSet()
            .reverseRangeByScoreWithScores(buildKey(grade), MIN_FEED_SCORE, maxScore, 0L, count);
    if (tuples == null) {
      return List.of();
    }
    return tuples.stream().map(this::toSortKey).toList();
  }

  @Override
  public void replace(String grade, List<HomeFeedSortKey> sortKeys) {
    String loadingKey = buildKey(grade) + LOADING_KEY_INFIX + UUID.randomUUID();
    redisTemplate.opsForZSet().add(loadingKey, LOADED_MARKER, LOADED_MARKER_SCORE);
    redisTemplate.expire(loadingKey, TTL);
    for (int from = 0; from < sortKeys.size(); from += ADD_BATCH_SIZE) {
      List<HomeFeedSortKey> batch =
          sortKeys.subList(from, Math.min(from + ADD_BATCH_SIZE, sortKeys.size()));
      redisTemplate.opsForZSet().add(loadingKey, toTuples(batch));
    }
    redisTemplate.rename(loadingKey, buildKey(grade));
  }

  @Override
  public void add(String grade, HomeFeedSortKey sortKey) {
    redisTemplate.opsForZSet().add(buildKey(grade), toMember(sortKey.feedId()), sortKey.score());
  }

  @Override
  public void remove(String grade, long feedId) {
    redisTemplate.opsForZSet().remove(buildKey(grade), toMember(feedId));
  }

  @Override
  public void remove(long feedId) {
    String member = toMember(feedId);
    FeedGrades.ALL.forEach(grade -> redisTemplate.opsForZSet().remove(buildKey(grade), member));
  }

  @Override
  public void removeAll(Collection<Long> feedIds) {
    if (feedIds.isEmpty()) {
      return;
    }
    Object[] members = feedIds.stream().map(this::toMember).toArray();
    FeedGrades.ALL.forEach(grade -> redisTemplate.opsForZSet().remove(buildKey(grade), members));
  }

  private Set<TypedTuple<String>> toTuples(List<HomeFeedSortKey> sortKeys) {
    return sortKeys.stream()
        .map(sortKey -> TypedTuple.of(toMember(sortKey.feedId()), (double) sortKey.score()))
        .collect(Collectors.toSet());
  }

  private HomeFeedSortKey toSortKey(TypedTuple<String> tuple) {
    return new HomeFeedSortKey(Long.parseLong(tuple.getValue()), tuple.getScore().longValue());
  }

  private String toMember(long feedId) {
    return MEMBER_FORMAT.formatted(feedId);
  }

  private String buildKey(String grade) {
    return KEY_PREFIX + grade;
  }
}
