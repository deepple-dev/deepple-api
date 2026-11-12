package kr.co.deepple.payment.command.application.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.infra.member.MemberCommandJpaRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOption;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOptionCommandRepository;
import kr.co.deepple.payment.command.domain.order.Order;
import kr.co.deepple.payment.command.domain.order.OrderCommandRepository;
import kr.co.deepple.payment.command.domain.order.PaymentMethod;
import kr.co.deepple.payment.command.domain.refund.NotificationType;
import kr.co.deepple.payment.command.domain.refund.RefundCommandRepository;
import kr.co.deepple.payment.fixture.HeartPurchaseOptionFixture;
import kr.co.deepple.payment.fixture.OrderFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("환불 통합 테스트")
class RefundServiceIntegrationTest extends IntegrationTestSupport {

  private static final long INITIAL_BALANCE = 100L;
  private static final long OPTION_AMOUNT = 45L;

  @Autowired private RefundService refundService;

  @Autowired private MemberCommandJpaRepository memberCommandJpaRepository;

  @Autowired private MemberHeartBalanceRepository memberHeartBalanceRepository;

  @Autowired private OrderCommandRepository orderCommandRepository;

  @Autowired private RefundCommandRepository refundCommandRepository;

  @Autowired private HeartPurchaseOptionCommandRepository heartPurchaseOptionCommandRepository;

  private HeartPurchaseOption option;

  @AfterEach
  void tearDown() {
    memberHeartBalanceRepository.deleteAll();
    refundCommandRepository.deleteAll();
    orderCommandRepository.deleteAll();
    heartPurchaseOptionCommandRepository.delete(option);
    memberCommandJpaRepository.deleteAll();
  }

  @Test
  @DisplayName("환불하면 환불한 만큼 구매 하트가 회수된다")
  void reclaimPurchaseHeartsWhenRefunded() {
    // given
    Member member = memberCommandJpaRepository.save(MemberFixture.builder().build());
    saveHeartBalance(member.getId());
    option =
        heartPurchaseOptionCommandRepository.save(
            HeartPurchaseOptionFixture.builder().amount(OPTION_AMOUNT).build());
    Order order =
        orderCommandRepository.save(OrderFixture.builder().memberId(member.getId()).build());

    // when
    refundService.processRefund(
        order.getTransactionId(),
        option.getProductId(),
        1,
        PaymentMethod.GOOGLE_PLAY,
        NotificationType.REFUND);

    // then
    assertThat(refundCommandRepository.count()).isEqualTo(1);
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> {
          long purchaseHeartBalance =
              memberHeartBalanceRepository
                  .findByMemberId(member.getId())
                  .orElseThrow()
                  .getPurchaseBalance();
          assertThat(purchaseHeartBalance).isEqualTo(INITIAL_BALANCE - OPTION_AMOUNT);
        });
  }

  private void saveHeartBalance(long memberId) {
    memberHeartBalanceRepository.save(
        MemberHeartBalanceFixture.builder()
            .memberId(memberId)
            .purchaseBalance(INITIAL_BALANCE)
            .build());
  }
}
