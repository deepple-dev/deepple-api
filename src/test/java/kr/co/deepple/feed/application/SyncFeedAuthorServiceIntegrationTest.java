package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.port.in.SyncFeedAuthorProfileCommand;
import kr.co.deepple.feed.application.port.in.SyncFeedAuthorUseCase;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedAuthorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("피드 작성자 사본 동기화 통합 테스트")
class SyncFeedAuthorServiceIntegrationTest extends IntegrationTestSupport {

  private static final long MEMBER_ID = 1L;
  private static final Instant JOINED_AT = Instant.parse("2026-01-02T00:00:00Z");
  private static final Instant PROFILE_SAVED_AT = Instant.parse("2026-01-02T00:00:01Z");
  private static final Instant APPROVED_AT = Instant.parse("2026-01-02T00:00:02Z");

  @Autowired private SyncFeedAuthorUseCase syncFeedAuthorUseCase;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Test
  @DisplayName("처음 온 이벤트로 회원 작성자 사본을 만든다")
  void createAuthorFromFirstEvent() {
    // when
    syncFeedAuthorUseCase.syncActivityStatus(MEMBER_ID, "INITIAL", JOINED_AT);

    // then
    FeedAuthor author = feedAuthorRepository.findByMemberId(MEMBER_ID).orElseThrow();
    assertThat(author.getAuthorType()).isEqualTo(FeedAuthorType.MEMBER);
    assertThat(author.getActivityStatus()).isEqualTo("INITIAL");
    assertThat(author.getActivityStatusUpdatedAt()).isEqualTo(JOINED_AT);
  }

  @Test
  @DisplayName("다른 항목의 이벤트가 순서가 뒤바뀌어 와도 모두 반영하고, 같은 항목의 오래된 이벤트는 무시한다")
  void applyEventsPerGroup() {
    // given
    syncFeedAuthorUseCase.syncActivityStatus(MEMBER_ID, "ACTIVE", APPROVED_AT);

    // when
    syncFeedAuthorUseCase.syncProfile(
        new SyncFeedAuthorProfileCommand(
            MEMBER_ID, "진저", "FEMALE", 1998, "SEOUL", "ENFP", PROFILE_SAVED_AT));
    syncFeedAuthorUseCase.syncGrade(MEMBER_ID, "SILVER", JOINED_AT);
    syncFeedAuthorUseCase.syncActivityStatus(MEMBER_ID, "INITIAL", JOINED_AT);

    // then
    FeedAuthor author = feedAuthorRepository.findByMemberId(MEMBER_ID).orElseThrow();
    assertThat(author.getNickname()).isEqualTo("진저");
    assertThat(author.getGrade()).isEqualTo("SILVER");
    assertThat(author.getActivityStatus()).isEqualTo("ACTIVE");
    assertThat(author.isActive()).isTrue();
  }

  @Test
  @DisplayName("같은 회원의 여러 항목 이벤트가 동시에 처리돼도 모두 성공하고 사본은 하나다")
  void syncConcurrently() throws InterruptedException {
    // given
    int threadCount = 20;
    CountDownLatch startSignal = new CountDownLatch(1);
    CountDownLatch doneSignal = new CountDownLatch(threadCount);
    AtomicInteger failureCount = new AtomicInteger();

    // when
    try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
      for (int i = 0; i < threadCount; i++) {
        int index = i;
        executor.submit(() -> {
          try {
            startSignal.await();
            if (index % 2 == 0) {
              syncFeedAuthorUseCase.syncActivityStatus(MEMBER_ID, "ACTIVE", APPROVED_AT);
            } else {
              syncFeedAuthorUseCase.syncPersonalityType(
                  MEMBER_ID, "GROWING_RUNNING_MATE", PROFILE_SAVED_AT);
            }
          } catch (Exception e) {
            failureCount.incrementAndGet();
          } finally {
            doneSignal.countDown();
          }
        });
      }
      startSignal.countDown();
      assertThat(doneSignal.await(30, TimeUnit.SECONDS)).isTrue();
    }

    // then
    assertThat(failureCount.get()).isZero();
    assertThat(feedAuthorRepository.count()).isEqualTo(1L);
    FeedAuthor author = feedAuthorRepository.findByMemberId(MEMBER_ID).orElseThrow();
    assertThat(author.getActivityStatus()).isEqualTo("ACTIVE");
    assertThat(author.getPersonalityType()).isEqualTo("GROWING_RUNNING_MATE");
  }
}
