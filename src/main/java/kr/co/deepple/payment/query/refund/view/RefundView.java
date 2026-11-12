package kr.co.deepple.payment.query.refund.view;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import kr.co.deepple.payment.command.domain.order.PaymentMethod;
import kr.co.deepple.payment.command.domain.refund.NotificationType;

public record RefundView(
    Long id,
    Long memberId,
    String transactionId,
    String productId,
    Integer quantity,
    Long refundAmount,
    PaymentMethod paymentMethod,
    NotificationType notificationType,
    LocalDateTime refundedAt) {
  @QueryProjection
  public RefundView {}
}
