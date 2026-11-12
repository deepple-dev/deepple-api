package kr.co.deepple.payment.fixture;

import kr.co.deepple.payment.command.domain.order.PaymentMethod;
import kr.co.deepple.payment.command.domain.refund.NotificationType;
import kr.co.deepple.payment.command.domain.refund.Refund;
import kr.co.deepple.payment.command.domain.refund.RefundDetail;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RefundFixture {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private Long id;
    private long orderId = 1L;
    private long memberId = 1L;
    private String transactionId;
    private String productId = "APP_ITEM_HEART_45";
    private int quantity = 1;
    private long refundAmount = 45L;
    private PaymentMethod paymentMethod = PaymentMethod.GOOGLE_PLAY;
    private NotificationType notificationType = NotificationType.REFUND;
    private LocalDateTime refundedAt = LocalDateTime.now();

    public static RefundFixture builder() {
        return new RefundFixture();
    }

    public Refund build() {
        String resolvedTransactionId =
            transactionId == null ? "refund-transaction-" + SEQUENCE.incrementAndGet() : transactionId;
        Refund refund = Refund.of(orderId, memberId, resolvedTransactionId,
            RefundDetail.of(productId, quantity, refundAmount), paymentMethod, notificationType, refundedAt);
        setField(refund, "id", id);
        return refund;
    }
}
