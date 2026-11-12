package kr.co.deepple.heart.application;

import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.application.port.in.SyncHeartMemberCommand;
import kr.co.deepple.heart.application.port.in.SyncHeartMemberUseCase;
import kr.co.deepple.heart.application.port.out.HeartMemberRepository;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.domain.Gender;
import kr.co.deepple.heart.domain.HeartMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("하트 회원 사본 동기화 통합 테스트")
class SyncHeartMemberServiceIntegrationTest extends IntegrationTestSupport {
    private static final long MEMBER_ID = 1L;
    private static final Instant JOINED_AT = Instant.parse("2026-01-02T00:00:00Z");
    private static final Instant PROFILE_SAVED_AT = Instant.parse("2026-01-02T00:00:01Z");

    @Autowired
    private SyncHeartMemberUseCase syncHeartMemberUseCase;

    @Autowired
    private HeartMemberRepository heartMemberRepository;

    @Autowired
    private MemberHeartBalanceRepository memberHeartBalanceRepository;

    @AfterEach
    void tearDown() {
        memberHeartBalanceRepository.deleteAll();
        heartMemberRepository.deleteAll();
    }

    @Test
    @DisplayName("처음 온 이벤트로 사본과 0짜리 잔액 행을 만든다")
    void createHeartMemberAndBalance() {
        // when
        syncHeartMemberUseCase.syncHeartMember(new SyncHeartMemberCommand(MEMBER_ID, null, false, JOINED_AT));

        // then
        HeartMember heartMember = heartMemberRepository.findByMemberId(MEMBER_ID).orElseThrow();
        assertThat(heartMember.getGender()).isNull();
        assertThat(heartMember.isVip()).isFalse();
        assertThat(heartMember.getSourceUpdatedAt()).isEqualTo(JOINED_AT);
        assertThat(memberHeartBalanceRepository.findByMemberId(MEMBER_ID).orElseThrow().getTotalBalance()).isZero();
    }

    @Test
    @DisplayName("더 오래된 이벤트가 나중에 와도 사본을 되돌리지 않는다")
    void ignoreOlderEvent() {
        // given
        syncHeartMemberUseCase.syncHeartMember(new SyncHeartMemberCommand(MEMBER_ID, "MALE", true, PROFILE_SAVED_AT));

        // when
        syncHeartMemberUseCase.syncHeartMember(new SyncHeartMemberCommand(MEMBER_ID, null, false, JOINED_AT));

        // then
        HeartMember heartMember = heartMemberRepository.findByMemberId(MEMBER_ID).orElseThrow();
        assertThat(heartMember.getGender()).isEqualTo(Gender.MALE);
        assertThat(heartMember.isVip()).isTrue();
        assertThat(heartMember.getSourceUpdatedAt()).isEqualTo(PROFILE_SAVED_AT);
    }

    @Test
    @DisplayName("가입 이벤트와 프로필 이벤트가 동시에 처리돼도 둘 다 성공하고 최신 값이 남는다")
    void keepLatestWhenSyncedConcurrently() throws InterruptedException {
        // given
        int threadCount = 20;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);
        AtomicInteger failureCount = new AtomicInteger();

        // when
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                SyncHeartMemberCommand command = i % 2 == 0
                    ? new SyncHeartMemberCommand(MEMBER_ID, null, false, JOINED_AT)
                    : new SyncHeartMemberCommand(MEMBER_ID, "MALE", false, PROFILE_SAVED_AT);
                executor.submit(() -> {
                    try {
                        startSignal.await();
                        syncHeartMemberUseCase.syncHeartMember(command);
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
        assertThat(heartMemberRepository.count()).isEqualTo(1L);
        assertThat(memberHeartBalanceRepository.count()).isEqualTo(1L);
        assertThat(heartMemberRepository.findByMemberId(MEMBER_ID).orElseThrow().getGender()).isEqualTo(Gender.MALE);
    }
}
