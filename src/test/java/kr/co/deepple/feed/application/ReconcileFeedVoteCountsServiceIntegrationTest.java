package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.port.out.FeedVoteCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteCountRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteRepository;
import kr.co.deepple.feed.domain.FeedVoteCount;
import kr.co.deepple.feed.fixture.FeedVoteCountFixture;
import kr.co.deepple.feed.fixture.FeedVoteFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@DisplayName("표 수 다시 세기 통합 테스트")
class ReconcileFeedVoteCountsServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired private ReconcileFeedVoteCountsService reconcileFeedVoteCountsService;

  @Autowired private FeedVoteRepository feedVoteRepository;

  @Autowired private FeedVoteCountRepository feedVoteCountRepository;

  @Autowired private FeedVoteCountCacheRepository feedVoteCountCacheRepository;

  @Autowired private StringRedisTemplate redisTemplate;

  @Test
  @DisplayName("투표 기록 수와 다르면 카운터와 feed_vote_counts를 기록 수로 고친다")
  void fixDriftedCounts() {
    // given
    saveVotes(1L, 11L, 3);
    saveVotes(1L, 12L, 1);
    feedVoteCountCacheRepository.saveAllIfAbsent(Map.of(1L, Map.of(11L, 2L, 12L, 1L)));
    feedVoteCountRepository.save(
        FeedVoteCountFixture.builder().optionId(11L).feedId(1L).voteCount(2L).build());

    // when
    reconcileFeedVoteCountsService.reconcile();

    // then
    assertThat(feedVoteCountCacheRepository.findAll(List.of(1L)))
        .containsEntry(1L, Map.of(11L, 3L, 12L, 1L));
    assertThat(findVoteCounts()).containsOnly(Map.entry(11L, 3L), Map.entry(12L, 1L));
  }

  @Test
  @DisplayName("기록이 없는데 표 수가 남은 선택지는 0으로 고친다")
  void fixToZeroWhenNoVotes() {
    // given
    feedVoteCountCacheRepository.saveAllIfAbsent(Map.of(1L, Map.of(11L, 5L, 12L, 0L)));
    feedVoteCountRepository.save(
        FeedVoteCountFixture.builder().optionId(11L).feedId(1L).voteCount(5L).build());

    // when
    reconcileFeedVoteCountsService.reconcile();

    // then
    assertThat(feedVoteCountCacheRepository.findAll(List.of(1L)))
        .containsEntry(1L, Map.of(11L, 0L, 12L, 0L));
    assertThat(findVoteCounts()).containsOnly(Map.entry(11L, 0L));
  }

  @Test
  @DisplayName("카운터만 틀리고 feed_vote_counts에 없는 선택지도 기록 수로 고친다")
  void fixCachedCountWithoutSavedRow() {
    // given
    saveVotes(1L, 11L, 1);
    feedVoteCountCacheRepository.saveAllIfAbsent(Map.of(1L, Map.of(11L, 1L, 12L, 4L)));

    // when
    reconcileFeedVoteCountsService.reconcile();

    // then
    assertThat(feedVoteCountCacheRepository.findAll(List.of(1L)))
        .containsEntry(1L, Map.of(11L, 1L, 12L, 0L));
  }

  @Test
  @DisplayName("카운터가 없으면 새로 만들지 않고 feed_vote_counts만 고친다")
  void doNotCreateMissingCounter() {
    // given
    saveVotes(1L, 11L, 2);

    // when
    reconcileFeedVoteCountsService.reconcile();

    // then
    assertThat(feedVoteCountCacheRepository.findAll(List.of(1L))).isEmpty();
    assertThat(findVoteCounts()).containsOnly(Map.entry(11L, 2L));
  }

  @Test
  @DisplayName("다시 고쳐도 카운터의 남은 만료 시간은 그대로다")
  void keepCounterTtl() {
    // given
    saveVotes(1L, 11L, 2);
    feedVoteCountCacheRepository.saveAllIfAbsent(Map.of(1L, Map.of(11L, 1L)));
    redisTemplate.expire("feed:vote-count:v1:1", Duration.ofSeconds(100));

    // when
    reconcileFeedVoteCountsService.reconcile();

    // then
    assertThat(redisTemplate.getExpire("feed:vote-count:v1:1")).isBetween(1L, 100L);
  }

  @Test
  @DisplayName("구간 크기보다 큰 피드 ID도 다시 센다")
  void reconcileBeyondFirstRange() {
    // given
    saveVotes(1L, 11L, 1);
    saveVotes(2500L, 25001L, 2);

    // when
    reconcileFeedVoteCountsService.reconcile();

    // then
    assertThat(findVoteCounts()).containsOnly(Map.entry(11L, 1L), Map.entry(25001L, 2L));
  }

  private void saveVotes(long feedId, long optionId, int count) {
    long firstMemberId = feedVoteRepository.count() + 1L;
    for (long memberId = firstMemberId; memberId < firstMemberId + count; memberId++) {
      feedVoteRepository.save(
          FeedVoteFixture.builder().feedId(feedId).memberId(memberId).optionId(optionId).build());
    }
  }

  private Map<Long, Long> findVoteCounts() {
    return feedVoteCountRepository.findAll().stream()
        .collect(Collectors.toMap(FeedVoteCount::getOptionId, FeedVoteCount::getVoteCount));
  }
}
