package deepple.deepple.payment.query.refund.view;

import com.querydsl.core.annotations.QueryProjection;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import deepple.deepple.payment.command.domain.refund.NotificationType;

import java.time.LocalDateTime;

public record RefundView(
    Long id,
    Long memberId,
    String transactionId,
    String productId,
    Integer quantity,
    Long refundAmount,
    PaymentMethod paymentMethod,
    NotificationType notificationType,
    LocalDateTime refundedAt
) {
    @QueryProjection
    public RefundView {
    }
}