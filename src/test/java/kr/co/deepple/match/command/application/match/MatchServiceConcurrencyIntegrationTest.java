package kr.co.deepple.match.command.application.match;

import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.application.port.out.HeartMemberRepository;
import kr.co.deepple.heart.application.port.out.HeartTransactionRepository;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.fixture.HeartMemberFixture;
import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import kr.co.deepple.match.command.domain.match.MatchContactType;
import kr.co.deepple.match.command.infra.match.MatchJpaRepository;
import kr.co.deepple.match.presentation.dto.MatchRequestDto;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.infra.member.MemberCommandJpaRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("매칭 요청 동시성 통합 테스트")
class MatchServiceConcurrencyIntegrationTest extends IntegrationTestSupport {

    private static final int THREAD_COUNT = 20;
    private static final long INITIAL_BALANCE = 2000L;

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchJpaRepository matchJpaRepository;

    @Autowired
    private MemberCommandJpaRepository memberCommandJpaRepository;

    @Autowired
    private HeartTransactionRepository heartTransactionRepository;

    @Autowired
    private MemberHeartBalanceRepository memberHeartBalanceRepository;

    @Autowired
    private HeartMemberRepository heartMemberRepository;

    @AfterEach
    void tearDown() {
        matchJpaRepository.deleteAll();
        heartTransactionRepository.deleteAll();
        memberHeartBalanceRepository.deleteAll();
        heartMemberRepository.deleteAll();
        memberCommandJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("두 회원이 서로에게 동시에 매칭을 요청해도 매칭은 하나만 생성된다")
    void createOnlyOneMatchWhenRequestedConcurrently() throws InterruptedException {
        // given
        Member memberA = memberCommandJpaRepository.save(MemberFixture.builder().build());
        saveHeartBalance(memberA.getId());
        Member memberB = memberCommandJpaRepository.save(MemberFixture.builder().build());
        saveHeartBalance(memberB.getId());
        long memberAId = memberA.getId();
        long memberBId = memberB.getId();

        // when
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(THREAD_COUNT);

        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                long requesterId = i % 2 == 0 ? memberAId : memberBId;
                long responderId = i % 2 == 0 ? memberBId : memberAId;
                executor.submit(() -> {
                    try {
                        startSignal.await();
                        matchService.request(requesterId,
                            new MatchRequestDto(responderId, "동시성 테스트", MatchContactType.PHONE_NUMBER.name()));
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

        // then
        assertThat(successCount.get() + failureCount.get()).isEqualTo(THREAD_COUNT);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(matchJpaRepository.count()).isEqualTo(1);
        assertThat(heartTransactionRepository.count()).isEqualTo(1L);
    }

    private void saveHeartBalance(long memberId) {
        heartMemberRepository.save(HeartMemberFixture.builder().memberId(memberId).build());
        memberHeartBalanceRepository.save(MemberHeartBalanceFixture.builder()
            .memberId(memberId)
            .purchaseBalance(INITIAL_BALANCE)
            .build());
    }
}
