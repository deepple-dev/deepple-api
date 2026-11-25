package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.HomeFeedSortKey;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.application.port.out.HomeFeedCacheRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@DisplayName("홈 목록 다시 채우기 통합 테스트")
class HomeFeedListLoaderIntegrationTest extends IntegrationTestSupport {

  private static final int REQUEST_COUNT = 10;
  private static final long ADD_WAIT_MILLIS = 500L;

  @Autowired private HomeFeedListLoader homeFeedListLoader;

  @Autowired private HomeFeedCacheRepository homeFeedCacheRepository;

  @Autowired private SyncHomeFeedCacheService syncHomeFeedCacheService;

  @MockitoSpyBean private FeedRepository feedRepository;

  @Test
  @DisplayName("목록이 빈 등급에 요청이 한꺼번에 와도 DB에서 한 번만 채우고, 피드가 없어도 빈 목록을 남긴다")
  void loadOnlyOnceForConcurrentRequests() throws Exception {
    // given
    CountDownLatch ready = new CountDownLatch(1);
    try (ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT)) {
      List<CompletableFuture<Void>> futures =
          IntStream.range(0, REQUEST_COUNT)
              .mapToObj(index ->
                  CompletableFuture.runAsync(
                      () -> {
                        await(ready);
                        homeFeedListLoader.loadIfNotLoaded("GOLD");
                      },
                      executor))
              .toList();

      // when
      ready.countDown();
      CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    }

    // then
    verify(feedRepository, times(1)).findHomeUserFeedSortKeys("GOLD");
    assertThat(homeFeedCacheRepository.isLoaded("GOLD")).isTrue();
    assertThat(homeFeedCacheRepository.findSortKeys("GOLD", Long.MAX_VALUE, 10)).isEmpty();
  }

  @Test
  @DisplayName("목록을 DB에서 읽은 뒤 발행된 피드도 다시 채운 목록에 남는다")
  void keepFeedPublishedWhileLoading() {
    // given
    Feed feed = feedRepository.save(TextFeedFixture.builder().grade("GOLD").build());
    AtomicReference<CompletableFuture<Void>> adding = new AtomicReference<>();
    try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
      doAnswer(
              invocation -> {
                adding.set(
                    CompletableFuture.runAsync(
                        () -> syncHomeFeedCacheService.addFeed(feed.getId()), executor));
                adding
                    .get()
                    .exceptionally(e -> null)
                    .completeOnTimeout(null, ADD_WAIT_MILLIS, TimeUnit.MILLISECONDS)
                    .join();
                return List.of();
              })
          .when(feedRepository)
          .findHomeUserFeedSortKeys("GOLD");

      // when
      homeFeedListLoader.loadIfNotLoaded("GOLD");
    }

    // then
    adding.get().join();
    assertThat(homeFeedCacheRepository.findSortKeys("GOLD", Long.MAX_VALUE, 10))
        .extracting(HomeFeedSortKey::feedId)
        .containsExactly(feed.getId());
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }
}
