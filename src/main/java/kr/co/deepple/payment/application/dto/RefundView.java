package kr.co.deepple.payment.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import kr.co.deepple.payment.domain.NotificationType;
import kr.co.deepple.payment.domain.PaymentMethod;

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
