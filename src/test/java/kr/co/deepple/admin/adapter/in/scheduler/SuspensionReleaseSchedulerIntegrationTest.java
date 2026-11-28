package kr.co.deepple.admin.adapter.in.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import kr.co.deepple.admin.application.port.out.SuspensionRepository;
import kr.co.deepple.admin.domain.SuspensionStatus;
import kr.co.deepple.admin.fixture.SuspensionFixture;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.MemberAccountStatus;
import kr.co.deepple.auth.fixture.MemberAccountFixture;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SuspensionReleaseSchedulerIntegrationTest extends IntegrationTestSupport {

  @Autowired private SuspensionReleaseScheduler suspensionReleaseScheduler;

  @Autowired private SuspensionRepository suspensionRepository;

  @Autowired private MemberAccountRepository memberAccountRepository;

  @Autowired private MemberRepository memberRepository;

  @Test
  @DisplayName("로그인하지 않아도 만료된 일시 정지는 해제되고, 만료 전 일시 정지와 영구 정지는 그대로 둔다.")
  void releaseOnlyExpiredTemporarySuspensions() {
    Instant now = Instant.now();
    long expired =
        saveSuspendedMember(
            MemberAccountStatus.SUSPENDED_TEMPORARILY,
            now.minus(1, ChronoUnit.MINUTES),
            ActivityStatus.SUSPENDED_TEMPORARILY);
    long notExpired =
        saveSuspendedMember(
            MemberAccountStatus.SUSPENDED_TEMPORARILY,
            now.plus(1, ChronoUnit.DAYS),
            ActivityStatus.SUSPENDED_TEMPORARILY);
    long permanent =
        saveSuspendedMember(
            MemberAccountStatus.SUSPENDED_PERMANENTLY, null, ActivityStatus.SUSPENDED_PERMANENTLY);

    suspensionReleaseScheduler.releaseExpired();

    assertThat(suspensionRepository.findByMemberId(expired)).isEmpty();
    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> {
          assertThat(memberAccountRepository.findById(expired).orElseThrow().getStatus())
              .isEqualTo(MemberAccountStatus.ACTIVE);
          Member member = memberRepository.findById(expired).orElseThrow();
          assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
          assertThat(member.isProfilePublic()).isTrue();
        });
    awaitEventProcessing();
    assertStillSuspended(notExpired, ActivityStatus.SUSPENDED_TEMPORARILY);
    assertStillSuspended(permanent, ActivityStatus.SUSPENDED_PERMANENTLY);
  }

  private void assertStillSuspended(long memberId, ActivityStatus activityStatus) {
    assertThat(suspensionRepository.findByMemberId(memberId)).isPresent();
    Member member = memberRepository.findById(memberId).orElseThrow();
    assertThat(member.getActivityStatus()).isEqualTo(activityStatus);
    assertThat(member.isProfilePublic()).isFalse();
  }

  private long saveSuspendedMember(
      MemberAccountStatus accountStatus, Instant suspendedUntil, ActivityStatus activityStatus) {
    MemberAccount account =
        memberAccountRepository.save(
            MemberAccountFixture.builder()
                .status(accountStatus)
                .suspendedUntil(suspendedUntil)
                .build());
    long memberId = account.getId();
    memberRepository.save(
        MemberFixture.builder()
            .id(memberId)
            .activityStatus(activityStatus)
            .profilePublic(false)
            .build());
    suspensionRepository.save(
        SuspensionFixture.builder()
            .memberId(memberId)
            .status(
                suspendedUntil == null ? SuspensionStatus.PERMANENT : SuspensionStatus.TEMPORARY)
            .expireAt(suspendedUntil)
            .build());
    return memberId;
  }
}
