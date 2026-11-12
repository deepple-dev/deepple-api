package kr.co.deepple.payment.command.domain.refund;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.payment.command.domain.order.PaymentMethod;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(
    name = "refunds",
    indexes = {@Index(name = "idx_refunds_member_id", columnList = "memberId")},
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_refunds_transaction_id", columnNames = "transactionId")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Refund extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(nullable = false)
  private Long memberId;

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
      @NonNull Long orderId,
      @NonNull Long memberId,
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
      Long orderId,
      Long memberId,
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

  public Integer getQuantity() {
    return refundDetail.getQuantity();
  }

  public Long getRefundAmount() {
    return refundDetail.getRefundAmount();
  }
}
