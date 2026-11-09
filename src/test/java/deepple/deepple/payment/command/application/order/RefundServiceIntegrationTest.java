package deepple.deepple.payment.command.application.order;

import deepple.deepple.common.support.IntegrationTestSupport;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.infra.member.MemberCommandJpaRepository;
import deepple.deepple.member.fixture.MemberFixture;
import deepple.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOption;
import deepple.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOptionCommandRepository;
import deepple.deepple.payment.command.domain.order.Order;
import deepple.deepple.payment.command.domain.order.OrderCommandRepository;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import deepple.deepple.payment.command.domain.refund.NotificationType;
import deepple.deepple.payment.command.domain.refund.RefundCommandRepository;
import deepple.deepple.payment.fixture.HeartPurchaseOptionFixture;
import deepple.deepple.payment.fixture.OrderFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("환불 통합 테스트")
class RefundServiceIntegrationTest extends IntegrationTestSupport {

    private static final long INITIAL_BALANCE = 100L;
    private static final long OPTION_AMOUNT = 45L;

    @Autowired
    private RefundService refundService;

    @Autowired
    private MemberCommandJpaRepository memberCommandJpaRepository;

    @Autowired
    private OrderCommandRepository orderCommandRepository;

    @Autowired
    private RefundCommandRepository refundCommandRepository;

    @Autowired
    private HeartPurchaseOptionCommandRepository heartPurchaseOptionCommandRepository;

    private HeartPurchaseOption option;

    @AfterEach
    void tearDown() {
        refundCommandRepository.deleteAll();
        orderCommandRepository.deleteAll();
        heartPurchaseOptionCommandRepository.delete(option);
        memberCommandJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("환불하면 환불한 만큼 구매 하트가 회수된다")
    void reclaimPurchaseHeartsWhenRefunded() {
        // given
        Member member = memberCommandJpaRepository.save(MemberFixture.builder()
            .purchaseHeartBalance(INITIAL_BALANCE)
            .build());
        option = heartPurchaseOptionCommandRepository.save(HeartPurchaseOptionFixture.builder()
            .amount(OPTION_AMOUNT)
            .build());
        Order order = orderCommandRepository.save(OrderFixture.builder()
            .memberId(member.getId())
            .build());

        // when
        refundService.processRefund(order.getTransactionId(), option.getProductId(), 1, PaymentMethod.GOOGLE_PLAY,
            NotificationType.REFUND);

        // then
        assertThat(refundCommandRepository.count()).isEqualTo(1);
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Member result = memberCommandJpaRepository.findById(member.getId()).orElseThrow();
            assertThat(result.getHeartBalance().getPurchaseHeartBalance()).isEqualTo(INITIAL_BALANCE - OPTION_AMOUNT);
        });
    }
}
