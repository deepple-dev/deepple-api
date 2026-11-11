package kr.co.deepple.payment.command.application.order;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.command.domain.hearttransaction.HeartTransactionCommandRepository;
import kr.co.deepple.member.command.domain.heartorder.ProcessedHeartOrderCommandRepository;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.infra.member.MemberCommandJpaRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.payment.command.domain.PaymentErrorCode;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOption;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOptionCommandRepository;
import kr.co.deepple.payment.command.domain.order.OrderCommandRepository;
import kr.co.deepple.payment.command.domain.order.PaymentMethod;
import kr.co.deepple.payment.fixture.HeartPurchaseOptionFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@DisplayName("결제 영수증 처리 동시성 통합 테스트")
class OrderServiceConcurrencyIntegrationTest extends IntegrationTestSupport {

    private static final int THREAD_COUNT = 20;
    private static final long INITIAL_BALANCE = 100L;
    private static final long OPTION_AMOUNT = 45L;
    private static final String TRANSACTION_ID = "duplicate-receipt-transaction";

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderCommandRepository orderCommandRepository;

    @Autowired
    private HeartPurchaseOptionCommandRepository heartPurchaseOptionCommandRepository;

    @Autowired
    private MemberCommandJpaRepository memberCommandJpaRepository;

    @Autowired
    private ProcessedHeartOrderCommandRepository processedHeartOrderCommandRepository;

    @Autowired
    private HeartTransactionCommandRepository heartTransactionCommandRepository;

    private HeartPurchaseOption option;
    private Member member;

    @BeforeEach
    void setUp() {
        member = memberCommandJpaRepository.save(MemberFixture.builder()
            .purchaseHeartBalance(INITIAL_BALANCE)
            .build());
        option = heartPurchaseOptionCommandRepository.save(HeartPurchaseOptionFixture.builder()
            .amount(OPTION_AMOUNT)
            .build());
    }

    @AfterEach
    void tearDown() {
        processedHeartOrderCommandRepository.deleteAll();
        orderCommandRepository.deleteAll();
        heartTransactionCommandRepository.deleteAll();
        heartPurchaseOptionCommandRepository.delete(option);
        memberCommandJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 회원이 같은 영수증을 다시 보내면 성공하고 하트는 한 번만 지급된다")
    void succeedWithoutRegrantWhenSameMemberRetries() {
        // given
        processReceipt(member.getId());

        // when & then
        assertThatCode(() -> processReceipt(member.getId())).doesNotThrowAnyException();
        assertThat(orderCommandRepository.count()).isEqualTo(1);
        assertGrantedOnce();
    }

    @Test
    @DisplayName("다른 회원이 같은 영수증을 보내면 ORDER_ALREADY_EXISTS 예외를 던진다")
    void throwWhenOtherMemberSendsSameReceipt() {
        // given
        processReceipt(member.getId());
        Member otherMember = memberCommandJpaRepository.save(MemberFixture.builder().build());

        // when & then
        assertThatThrownBy(() -> processReceipt(otherMember.getId()))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.ORDER_ALREADY_EXISTS);
        assertGrantedOnce();
    }

    @Test
    @DisplayName("같은 회원이 같은 영수증을 동시에 보내도 모두 성공하고 주문은 하나, 하트는 한 번만 지급된다")
    void succeedAllAndGrantOnceWhenSameReceiptSentConcurrently() throws InterruptedException {
        // given
        long memberId = member.getId();

        // when
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(THREAD_COUNT);

        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(() -> {
                    try {
                        startSignal.await();
                        processReceipt(memberId);
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
        assertThat(successCount.get()).isEqualTo(THREAD_COUNT);
        assertThat(failureCount.get()).isZero();
        assertThat(orderCommandRepository.count()).isEqualTo(1);
        assertGrantedOnce();
    }

    private void processReceipt(long memberId) {
        orderService.processReceipt(memberId, TRANSACTION_ID, option.getProductId(), 1, PaymentMethod.GOOGLE_PLAY);
    }

    private void assertGrantedOnce() {
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
            assertThat(purchaseHeartBalance()).isEqualTo(INITIAL_BALANCE + OPTION_AMOUNT));
        await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(purchaseHeartBalance()).isEqualTo(INITIAL_BALANCE + OPTION_AMOUNT);
            assertThat(heartTransactionCommandRepository.count()).isEqualTo(1);
        });
    }

    private long purchaseHeartBalance() {
        return memberCommandJpaRepository.findById(member.getId()).orElseThrow()
            .getHeartBalance().getPurchaseHeartBalance();
    }
}
