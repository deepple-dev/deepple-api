package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import kr.co.deepple.feed.domain.FeedCount;
import kr.co.deepple.feed.fixture.FeedCountFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("좋아요 수 write-behind 통합 테스트")
class FlushFeedLikeCountsServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired private FlushFeedLikeCountsService flushFeedLikeCountsService;

  @Autowired private FeedLikeCountCacheRepository feedLikeCountCacheRepository;

  @Autowired private FeedCountRepository feedCountRepository;

  @Test
  @DisplayName("바뀐 피드의 지금 카운터 값을 feed_counts에 덮어쓴다")
  void flushChangedCounts() {
    // given
    feedLikeCountCacheRepository.saveAllIfAbsent(Map.of(1L, 10L, 2L, 0L, 3L, 5L));
    feedCountRepository.save(FeedCountFixture.builder().feedId(1L).likeCount(10L).build());
    feedLikeCountCacheRepository.incrementIfPresent(1L);
    feedLikeCountCacheRepository.incrementIfPresent(2L);
    feedLikeCountCacheRepository.incrementIfPresent(2L);

    // when
    flushFeedLikeCountsService.flush();

    // then
    assertThat(findLikeCounts()).containsOnly(Map.entry(1L, 11L), Map.entry(2L, 2L));
  }

  @Test
  @DisplayName("반영한 피드는 다시 바뀌기 전까지 다시 쓰지 않는다")
  void flushOnlyOnce() {
    // given
    feedLikeCountCacheRepository.saveAllIfAbsent(Map.of(1L, 0L));
    feedLikeCountCacheRepository.incrementIfPresent(1L);
    flushFeedLikeCountsService.flush();
    feedCountRepository.save(FeedCountFixture.builder().feedId(1L).likeCount(99L).build());

    // when
    flushFeedLikeCountsService.flush();

    // then
    assertThat(findLikeCounts()).containsOnly(Map.entry(1L, 99L));
  }

  @Test
  @DisplayName("카운터가 지워진 피드는 건너뛴다")
  void skipDeletedCounter() {
    // given
    feedLikeCountCacheRepository.saveAllIfAbsent(Map.of(1L, 0L));
    feedLikeCountCacheRepository.incrementIfPresent(1L);
    feedLikeCountCacheRepository.delete(1L);

    // when
    flushFeedLikeCountsService.flush();

    // then
    assertThat(feedCountRepository.count()).isZero();
  }

  @Test
  @DisplayName("한 번에 꺼내는 개수보다 많이 바뀌어도 모두 반영한다")
  void flushMoreThanBatchSize() {
    // given
    Map<Long, Long> counters =
        LongStream.rangeClosed(1L, 501L)
            .boxed()
            .collect(Collectors.toMap(Function.identity(), feedId -> 0L));
    feedLikeCountCacheRepository.saveAllIfAbsent(counters);
    counters.keySet().forEach(feedLikeCountCacheRepository::incrementIfPresent);

    // when
    flushFeedLikeCountsService.flush();

    // then
    assertThat(feedCountRepository.count()).isEqualTo(501L);
  }

  private Map<Long, Long> findLikeCounts() {
    return feedCountRepository.findAll().stream()
        .collect(Collectors.toMap(FeedCount::getFeedId, FeedCount::getLikeCount));
  }
}
