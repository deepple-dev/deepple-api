package kr.co.deepple.admin.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongConsumer;
import kr.co.deepple.admin.application.port.out.ScreeningCommandRepository;
import kr.co.deepple.admin.domain.Screening;
import kr.co.deepple.admin.domain.ScreeningStatus;
import kr.co.deepple.admin.fixture.ScreeningFixture;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.member.adapter.out.persistence.MemberCommandJpaRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("심사 생성 동시성 통합 테스트")
class ScreeningServiceConcurrencyIntegrationTest extends IntegrationTestSupport {

  private static final int THREAD_COUNT = 20;

  @Autowired private ScreeningService screeningService;

  @Autowired private ScreeningCommandRepository screeningCommandRepository;

  @Autowired private MemberCommandJpaRepository memberCommandJpaRepository;

  private Member member;

  @BeforeEach
  void setUp() {
    member = memberCommandJpaRepository.save(MemberFixture.builder().build());
  }

  @AfterEach
  void tearDown() {
    screeningCommandRepository.deleteAll();
    memberCommandJpaRepository.deleteAll();
  }

  @Test
  @DisplayName("최초 심사 생성이 동시에 들어와도 모두 정상 종료하고 심사는 하나만 생성된다")
  void createOnlyOneScreeningWhenCreatedConcurrently() throws InterruptedException {
    // when
    ConcurrentResult result = runConcurrently(screeningService::createIfAbsent);

    // then
    assertThat(result.successCount()).isEqualTo(THREAD_COUNT);
    assertThat(screeningCommandRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("대기 중인 심사가 있으면 재심사 요청은 새 심사를 만들지 않고 정상 종료한다")
  void skipRescreenWhenPendingScreeningExists() {
    // given
    screeningService.rescreen(member.getId());

    // when
    screeningService.rescreen(member.getId());

    // then
    assertThat(screeningCommandRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("재심사 요청이 동시에 들어와도 모두 정상 종료하고 대기 중인 심사는 하나만 생성된다")
  void createOnlyOnePendingScreeningWhenRescreenedConcurrently() throws InterruptedException {
    // when
    ConcurrentResult result = runConcurrently(screeningService::rescreen);

    // then
    assertThat(result.successCount()).isEqualTo(THREAD_COUNT);
    assertThat(screeningCommandRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("반려된 심사만 있으면 재심사 요청은 새 대기 심사를 만든다")
  void createPendingScreeningWhenOnlyRejectedScreeningExists() {
    // given
    Screening rejected =
        screeningCommandRepository.save(
            ScreeningFixture.builder()
                .memberId(member.getId())
                .status(ScreeningStatus.REJECTED)
                .build());

    // when
    screeningService.rescreen(member.getId());

    // then
    assertThat(screeningCommandRepository.count()).isEqualTo(2);
    assertThat(screeningCommandRepository.findById(rejected.getId()).orElseThrow().getStatus())
        .isEqualTo(ScreeningStatus.REJECTED);
  }

  private ConcurrentResult runConcurrently(LongConsumer action) throws InterruptedException {
    long memberId = member.getId();
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger failureCount = new AtomicInteger();
    CountDownLatch startSignal = new CountDownLatch(1);
    CountDownLatch doneSignal = new CountDownLatch(THREAD_COUNT);

    try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
      for (int i = 0; i < THREAD_COUNT; i++) {
        executor.submit(() -> {
          try {
            startSignal.await();
            action.accept(memberId);
            successCount.incrementAndGet();
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
    return new ConcurrentResult(successCount.get(), failureCount.get());
  }

  private record ConcurrentResult(int successCount, int failureCount) {}
}
