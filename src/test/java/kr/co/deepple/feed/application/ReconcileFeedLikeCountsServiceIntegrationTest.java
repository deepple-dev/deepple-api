package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedLikeCount;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeRepository;
import kr.co.deepple.feed.domain.FeedCount;
import kr.co.deepple.feed.fixture.FeedCountFixture;
import kr.co.deepple.feed.fixture.FeedLikeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@DisplayName("좋아요 수 다시 세기 통합 테스트")
class ReconcileFeedLikeCountsServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired private ReconcileFeedLikeCountsService reconcileFeedLikeCountsService;

  @MockitoSpyBean private FeedLikeRepository feedLikeRepository;

  @Autowired private FeedCountRepository feedCountRepository;

  @Autowired private FeedLikeCountCacheRepository feedLikeCountCacheRepository;

  @Test
  @DisplayName("좋아요 기록 수와 다르면 카운터와 feed_counts를 기록 수로 고친다")
  void fixDriftedCounts() {
    // given
    saveLikes(1L, 3);
    feedLikeCountCacheRepository.saveAllIfAbsent(Map.of(1L, 1L));
    feedCountRepository.save(FeedCountFixture.builder().feedId(1L).likeCount(1L).build());

    // when
    reconcileFeedLikeCountsService.reconcile();

    // then
    assertThat(feedLikeCountCacheRepository.findAll(List.of(1L))).containsEntry(1L, 3L);
    assertThat(findLikeCounts()).containsEntry(1L, 3L);
  }

  @Test
  @DisplayName("다시 세는 사이에 들어온 좋아요는 카운터에서 지우지 않는다")
  void keepLikeAddedWhileReconciling() {
    // given
    saveLikes(1L, 2);
    feedLikeCountCacheRepository.saveAllIfAbsent(Map.of(1L, 2L));
    doAnswer(
            invocation -> {
              feedLikeRepository.save(FeedLikeFixture.builder().feedId(1L).memberId(3L).build());
              feedLikeCountCacheRepository.incrementIfPresent(1L);
              return List.of(new FeedLikeCount(1L, 2L));
            })
        .when(feedLikeRepository)
        .countByFeedIdBetween(anyLong(), anyLong());

    // when
    reconcileFeedLikeCountsService.reconcile();

    // then
    assertThat(feedLikeCountCacheRepository.findAll(List.of(1L))).containsEntry(1L, 3L);
  }

  @Test
  @DisplayName("기록이 모두 사라진 피드는 0으로 고친다")
  void fixToZeroWhenNoLikes() {
    // given
    feedLikeCountCacheRepository.saveAllIfAbsent(Map.of(1L, 5L));
    feedCountRepository.save(FeedCountFixture.builder().feedId(1L).likeCount(5L).build());

    // when
    reconcileFeedLikeCountsService.reconcile();

    // then
    assertThat(feedLikeCountCacheRepository.findAll(List.of(1L))).containsEntry(1L, 0L);
    assertThat(findLikeCounts()).containsEntry(1L, 0L);
  }

  @Test
  @DisplayName("카운터가 없으면 새로 만들지 않고 feed_counts만 고친다")
  void doNotCreateMissingCounter() {
    // given
    saveLikes(1L, 2);

    // when
    reconcileFeedLikeCountsService.reconcile();

    // then
    assertThat(feedLikeCountCacheRepository.findAll(List.of(1L))).isEmpty();
    assertThat(findLikeCounts()).containsEntry(1L, 2L);
  }

  @Test
  @DisplayName("구간 크기보다 큰 피드 ID도 다시 센다")
  void reconcileBeyondFirstRange() {
    // given
    saveLikes(1L, 1);
    saveLikes(2500L, 2);

    // when
    reconcileFeedLikeCountsService.reconcile();

    // then
    assertThat(findLikeCounts()).containsOnly(Map.entry(1L, 1L), Map.entry(2500L, 2L));
  }

  private void saveLikes(long feedId, int count) {
    for (long memberId = 1L; memberId <= count; memberId++) {
      feedLikeRepository.save(FeedLikeFixture.builder().feedId(feedId).memberId(memberId).build());
    }
  }

  private Map<Long, Long> findLikeCounts() {
    return feedCountRepository.findAll().stream()
        .collect(Collectors.toMap(FeedCount::getFeedId, FeedCount::getLikeCount));
  }
}
