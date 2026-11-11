package deepple.deepple.member.command.infra.member;

import deepple.deepple.common.support.IntegrationTestSupport;
import deepple.deepple.heart.command.domain.hearttransaction.HeartTransactionCommandRepository;
import deepple.deepple.member.command.domain.heartorder.ProcessedHeartOrderCommandRepository;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.fixture.MemberFixture;
import deepple.deepple.payment.command.domain.order.event.HeartPurchasedEvent;
import deepple.deepple.payment.command.domain.order.event.HeartRefundedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("하트 지급·회수 이벤트 멱등성 통합 테스트")
class HeartBalanceEventIdempotencyIntegrationTest extends IntegrationTestSupport {

    private static final long INITIAL_BALANCE = 100L;
    private static final long AMOUNT = 45L;
    private static final long ORDER_ID = 1L;

    @Autowired
    private HeartPurchasedEventHandler heartPurchasedEventHandler;

    @Autowired
    private MemberEventHandler memberEventHandler;

    @Autowired
    private MemberCommandJpaRepository memberCommandJpaRepository;

    @Autowired
    private HeartTransactionCommandRepository heartTransactionCommandRepository;

    @Autowired
    private ProcessedHeartOrderCommandRepository processedHeartOrderCommandRepository;

    @AfterEach
    void tearDown() {
        processedHeartOrderCommandRepository.deleteAll();
        heartTransactionCommandRepository.deleteAll();
        memberCommandJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 하트 지급 이벤트를 두 번 처리해도 하트는 한 번만 지급된다")
    void grantOnceWhenSamePurchasedEventHandledTwice() {
        // given
        Member member = saveMember();
        HeartPurchasedEvent event = HeartPurchasedEvent.of(member.getId(), ORDER_ID, AMOUNT);

        // when
        heartPurchasedEventHandler.handle(event);
        heartPurchasedEventHandler.handle(event);

        // then
        assertBalanceStays(member, INITIAL_BALANCE + AMOUNT);
    }

    @Test
    @DisplayName("같은 하트 회수 이벤트를 두 번 처리해도 하트는 한 번만 회수된다")
    void reclaimOnceWhenSameRefundedEventHandledTwice() {
        // given
        Member member = saveMember();
        HeartRefundedEvent event = HeartRefundedEvent.of(member.getId(), ORDER_ID, AMOUNT, "refund-transaction");

        // when
        memberEventHandler.handle(event);
        memberEventHandler.handle(event);

        // then
        assertBalanceStays(member, INITIAL_BALANCE - AMOUNT);
    }

    private Member saveMember() {
        return memberCommandJpaRepository.save(MemberFixture.builder()
            .purchaseHeartBalance(INITIAL_BALANCE)
            .build());
    }

    private void assertBalanceStays(Member member, long expected) {
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
            assertThat(purchaseHeartBalance(member)).isEqualTo(expected));
        await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(2)).untilAsserted(() ->
            assertThat(purchaseHeartBalance(member)).isEqualTo(expected));
    }

    private long purchaseHeartBalance(Member member) {
        return memberCommandJpaRepository.findById(member.getId()).orElseThrow()
            .getHeartBalance().getPurchaseHeartBalance();
    }
}
