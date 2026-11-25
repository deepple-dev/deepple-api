package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedCommentCount;
import kr.co.deepple.feed.application.port.out.FeedCommentCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.domain.FeedCount;
import kr.co.deepple.feed.fixture.FeedCommentFixture;
import kr.co.deepple.feed.fixture.FeedCountFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@DisplayName("댓글 수 다시 세기 통합 테스트")
class ReconcileFeedCommentCountsServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired private ReconcileFeedCommentCountsService reconcileFeedCommentCountsService;

  @MockitoSpyBean private FeedCommentRepository feedCommentRepository;

  @Autowired private FeedCountRepository feedCountRepository;

  @Autowired private FeedCommentCountCacheRepository feedCommentCountCacheRepository;

  @Test
  @DisplayName("댓글 수와 다르면 카운터와 feed_counts를 댓글 수로 고친다")
  void fixDriftedCounts() {
    // given
    saveComments(1L, 3);
    feedCommentCountCacheRepository.saveAllIfAbsent(Map.of(1L, 1L));
    feedCountRepository.save(FeedCountFixture.builder().feedId(1L).commentCount(1L).build());

    // when
    reconcileFeedCommentCountsService.reconcile();

    // then
    assertThat(feedCommentCountCacheRepository.findAll(List.of(1L))).containsEntry(1L, 3L);
    assertThat(findCommentCounts()).containsEntry(1L, 3L);
  }

  @Test
  @DisplayName("다시 세는 사이에 들어온 댓글은 카운터에서 지우지 않는다")
  void keepCommentAddedWhileReconciling() {
    // given
    saveComments(1L, 2);
    feedCommentCountCacheRepository.saveAllIfAbsent(Map.of(1L, 2L));
    doAnswer(
            invocation -> {
              feedCommentRepository.save(FeedCommentFixture.builder().feedId(1L).build());
              feedCommentCountCacheRepository.incrementIfPresent(1L);
              return List.of(new FeedCommentCount(1L, 2L));
            })
        .when(feedCommentRepository)
        .countByFeedIdBetween(anyLong(), anyLong());

    // when
    reconcileFeedCommentCountsService.reconcile();

    // then
    assertThat(feedCommentCountCacheRepository.findAll(List.of(1L))).containsEntry(1L, 3L);
  }

  @Test
  @DisplayName("댓글 행이 없는 피드는 0으로 고친다")
  void fixToZeroWhenNoComments() {
    // given
    feedCommentCountCacheRepository.saveAllIfAbsent(Map.of(1L, 5L));
    feedCountRepository.save(FeedCountFixture.builder().feedId(1L).commentCount(5L).build());

    // when
    reconcileFeedCommentCountsService.reconcile();

    // then
    assertThat(feedCommentCountCacheRepository.findAll(List.of(1L))).containsEntry(1L, 0L);
    assertThat(findCommentCounts()).containsEntry(1L, 0L);
  }

  @Test
  @DisplayName("삭제된 댓글도 센다")
  void countDeletedComments() {
    // given
    saveComments(1L, 1);
    feedCommentRepository.save(
        FeedCommentFixture.builder().feedId(1L).deletedAt(LocalDateTime.now()).build());

    // when
    reconcileFeedCommentCountsService.reconcile();

    // then
    assertThat(findCommentCounts()).containsEntry(1L, 2L);
  }

  @Test
  @DisplayName("카운터가 없으면 새로 만들지 않고 feed_counts만 고친다")
  void doNotCreateMissingCounter() {
    // given
    saveComments(1L, 2);

    // when
    reconcileFeedCommentCountsService.reconcile();

    // then
    assertThat(feedCommentCountCacheRepository.findAll(List.of(1L))).isEmpty();
    assertThat(findCommentCounts()).containsEntry(1L, 2L);
  }

  @Test
  @DisplayName("구간 크기보다 큰 피드 ID도 다시 센다")
  void reconcileBeyondFirstRange() {
    // given
    saveComments(1L, 1);
    saveComments(2500L, 2);

    // when
    reconcileFeedCommentCountsService.reconcile();

    // then
    assertThat(findCommentCounts()).containsOnly(Map.entry(1L, 1L), Map.entry(2500L, 2L));
  }

  private void saveComments(long feedId, int count) {
    for (int i = 0; i < count; i++) {
      feedCommentRepository.save(FeedCommentFixture.builder().feedId(feedId).build());
    }
  }

  private Map<Long, Long> findCommentCounts() {
    return feedCountRepository.findAll().stream()
        .collect(Collectors.toMap(FeedCount::getFeedId, FeedCount::getCommentCount));
  }
}
