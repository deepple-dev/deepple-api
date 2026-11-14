package kr.co.deepple.mission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.adapter.in.event.HeartMissionEventHandler;
import kr.co.deepple.heart.application.port.out.HeartTransactionRepository;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.mission.application.port.out.MemberMissionRepository;
import kr.co.deepple.mission.domain.MemberMission;
import kr.co.deepple.mission.event.MemberMissionCompletedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("미션 수행·보상 동시성 통합 테스트")
class MemberMissionServiceConcurrencyIntegrationTest extends IntegrationTestSupport {

  private static final int THREAD_COUNT = 20;
  private static final int LIKE_REPEATABLE_COUNT = 2;
  private static final long LIKE_REWARDED_HEART = 2L;

  @Autowired private MemberMissionService memberMissionService;

  @Autowired private HeartMissionEventHandler heartMissionEventHandler;

  @Autowired private MemberMissionRepository memberMissionRepository;

  @Autowired private MemberRepository memberRepository;

  @Autowired private MemberHeartBalanceRepository memberHeartBalanceRepository;

  @Autowired private HeartTransactionRepository heartTransactionRepository;

  @AfterEach
  void tearDown() {
    memberMissionRepository.deleteAll();
    heartTransactionRepository.deleteAll();
    memberHeartBalanceRepository.deleteAll();
    memberRepository.deleteAll();
  }

  @Test
  @DisplayName("같은 회원이 일일 미션을 동시에 수행해도 회원 미션은 하나이고 반복 횟수만큼만 보상한다")
  void rewardOnlyRepeatableCountWhenExecutedConcurrently() throws InterruptedException {
    // given
    long memberId = saveMaleMemberWithBalance();

    // when
    AtomicInteger failureCount = new AtomicInteger();
    runConcurrently(
        () -> memberMissionService.executeMissionsByAction(memberId, "LIKE"), failureCount);

    // then
    assertThat(failureCount.get()).isZero();
    List<MemberMission> memberMissions = memberMissionRepository.findAll();
    assertThat(memberMissions).hasSize(1);
    MemberMission memberMission = memberMissions.getFirst();
    assertThat(memberMission.getPeriod()).isEqualTo(LocalDate.now().toString());
    assertThat(memberMission.getSuccessCount()).isEqualTo(LIKE_REPEATABLE_COUNT);
    assertThat(memberMission.isCompleted()).isTrue();
    assertMissionBalanceStays(memberId, LIKE_REPEATABLE_COUNT * LIKE_REWARDED_HEART);
    assertThat(heartTransactionRepository.count()).isEqualTo(LIKE_REPEATABLE_COUNT);
  }

  @Test
  @DisplayName("같은 미션 보상 이벤트를 두 번 처리해도 하트는 한 번만 지급된다")
  void addOnceWhenSameCompletedEventHandledTwice() {
    // given
    long memberId = saveMaleMemberWithBalance();
    MemberMissionCompletedEvent event =
        MemberMissionCompletedEvent.of(memberId, 1L, 1, "닉네임", LIKE_REWARDED_HEART, "미션");

    // when
    heartMissionEventHandler.handle(event);
    heartMissionEventHandler.handle(event);

    // then
    assertMissionBalanceStays(memberId, LIKE_REWARDED_HEART);
    assertThat(heartTransactionRepository.count()).isEqualTo(1L);
  }

  private long saveMaleMemberWithBalance() {
    long memberId =
        memberRepository.save(MemberFixture.builder().gender(Gender.MALE).build()).getId();
    memberHeartBalanceRepository.save(
        MemberHeartBalanceFixture.builder().memberId(memberId).build());
    return memberId;
  }

  private void assertMissionBalanceStays(long memberId, long expected) {
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> assertThat(missionBalance(memberId)).isEqualTo(expected));
    await()
        .during(Duration.ofSeconds(1))
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(() -> assertThat(missionBalance(memberId)).isEqualTo(expected));
  }

  private long missionBalance(long memberId) {
    return memberHeartBalanceRepository.findByMemberId(memberId).orElseThrow().getMissionBalance();
  }

  private void runConcurrently(Runnable task, AtomicInteger failureCount)
      throws InterruptedException {
    CountDownLatch startSignal = new CountDownLatch(1);
    CountDownLatch doneSignal = new CountDownLatch(THREAD_COUNT);
    try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
      for (int i = 0; i < THREAD_COUNT; i++) {
        executor.submit(() -> {
          try {
            startSignal.await();
            task.run();
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
  }
}
