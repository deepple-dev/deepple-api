package kr.co.deepple.payment.command.domain.refund;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.payment.command.domain.order.PaymentMethod;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private long orderId;

  @Column(nullable = false)
  private long memberId;

  @Column(nullable = false)
  private String transactionId;

  @Embedded private RefundDetail refundDetail;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private NotificationType notificationType;

  @Column(nullable = false)
  private LocalDateTime refundedAt;

  private Refund(
      long orderId,
      long memberId,
      @NonNull String transactionId,
      @NonNull RefundDetail refundDetail,
      @NonNull PaymentMethod paymentMethod,
      @NonNull NotificationType notificationType,
      @NonNull LocalDateTime refundedAt) {
    this.orderId = orderId;
    this.memberId = memberId;
    this.transactionId = transactionId;
    this.refundDetail = refundDetail;
    this.paymentMethod = paymentMethod;
    this.notificationType = notificationType;
    this.refundedAt = refundedAt;
  }

  public static Refund of(
      long orderId,
      long memberId,
      String transactionId,
      RefundDetail refundDetail,
      PaymentMethod paymentMethod,
      NotificationType notificationType,
      LocalDateTime refundedAt) {
    return new Refund(
        orderId,
        memberId,
        transactionId,
        refundDetail,
        paymentMethod,
        notificationType,
        refundedAt);
  }

  public String getProductId() {
    return refundDetail.getProductId();
  }

  public int getQuantity() {
    return refundDetail.getQuantity();
  }

  public long getRefundAmount() {
    return refundDetail.getRefundAmount();
  }
}
