package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.port.out.FeedVoteCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteCountRepository;
import kr.co.deepple.feed.domain.FeedVoteCount;
import kr.co.deepple.feed.fixture.FeedVoteCountFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("표 수 write-behind 통합 테스트")
class FlushFeedVoteCountsServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired private FlushFeedVoteCountsService flushFeedVoteCountsService;

  @Autowired private FeedVoteCountCacheRepository feedVoteCountCacheRepository;

  @Autowired private FeedVoteCountRepository feedVoteCountRepository;

  @Test
  @DisplayName("바뀐 피드의 지금 선택지별 표 수를 feed_vote_counts에 덮어쓴다")
  void flushChangedCounts() {
    // given
    feedVoteCountCacheRepository.saveAllIfAbsent(
        Map.of(1L, Map.of(11L, 3L, 12L, 0L), 2L, Map.of(21L, 0L, 22L, 0L), 3L, Map.of(31L, 5L)));
    feedVoteCountRepository.save(
        FeedVoteCountFixture.builder().optionId(11L).feedId(1L).voteCount(3L).build());
    feedVoteCountCacheRepository.incrementIfPresent(1L, 12L);
    feedVoteCountCacheRepository.incrementIfPresent(2L, 22L);
    feedVoteCountCacheRepository.incrementIfPresent(2L, 22L);

    // when
    flushFeedVoteCountsService.flush();

    // then
    assertThat(findVoteCounts())
        .containsOnly(
            Map.entry(11L, 3L), Map.entry(12L, 1L), Map.entry(21L, 0L), Map.entry(22L, 2L));
  }

  @Test
  @DisplayName("반영한 피드는 다시 바뀌기 전까지 다시 쓰지 않는다")
  void flushOnlyOnce() {
    // given
    feedVoteCountCacheRepository.saveAllIfAbsent(Map.of(1L, Map.of(11L, 0L)));
    feedVoteCountCacheRepository.incrementIfPresent(1L, 11L);
    flushFeedVoteCountsService.flush();
    feedVoteCountRepository.save(
        FeedVoteCountFixture.builder().optionId(11L).feedId(1L).voteCount(99L).build());

    // when
    flushFeedVoteCountsService.flush();

    // then
    assertThat(findVoteCounts()).containsOnly(Map.entry(11L, 99L));
  }

  @Test
  @DisplayName("카운터가 지워진 피드는 건너뛴다")
  void skipDeletedCounter() {
    // given
    feedVoteCountCacheRepository.saveAllIfAbsent(Map.of(1L, Map.of(11L, 0L)));
    feedVoteCountCacheRepository.incrementIfPresent(1L, 11L);
    feedVoteCountCacheRepository.delete(1L);

    // when
    flushFeedVoteCountsService.flush();

    // then
    assertThat(feedVoteCountRepository.count()).isZero();
  }

  @Test
  @DisplayName("한 번에 꺼내는 개수보다 많이 바뀌어도 모두 반영한다")
  void flushMoreThanBatchSize() {
    // given
    Map<Long, Map<Long, Long>> counters =
        LongStream.rangeClosed(1L, 501L)
            .boxed()
            .collect(Collectors.toMap(Function.identity(), feedId -> Map.of(feedId * 10, 0L)));
    feedVoteCountCacheRepository.saveAllIfAbsent(counters);
    counters
        .keySet()
        .forEach(feedId -> feedVoteCountCacheRepository.incrementIfPresent(feedId, feedId * 10));

    // when
    flushFeedVoteCountsService.flush();

    // then
    assertThat(feedVoteCountRepository.count()).isEqualTo(501L);
  }

  private Map<Long, Long> findVoteCounts() {
    return feedVoteCountRepository.findAll().stream()
        .collect(Collectors.toMap(FeedVoteCount::getOptionId, FeedVoteCount::getVoteCount));
  }
}
