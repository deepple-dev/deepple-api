package kr.co.deepple.payment.command.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.command.domain.PaymentErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(
    name = "orders",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"transactionId", "paymentMethod"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter private Long memberId;

  @Getter private String transactionId;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  @Getter
  private OrderStatus status;

  private Order(Long memberId, String transactionId, PaymentMethod paymentMethod) {
    setMemberId(memberId);
    setTransactionId(transactionId);
    setPaymentMethod(paymentMethod);
    setStatus(OrderStatus.PAID);
  }

  public static Order of(Long memberId, String transactionId, PaymentMethod paymentMethod) {
    return new Order(memberId, transactionId, paymentMethod);
  }

  public void refund() {
    if (!isRefundable()) {
      throw new DeeppleException(
          PaymentErrorCode.ORDER_NOT_REFUNDABLE, "PAID 상태의 주문만 환불할 수 있습니다. orderStatus=" + status);
    }
    setStatus(OrderStatus.REFUNDED);
  }

  public boolean isOrderedBy(long memberId) {
    return this.memberId == memberId;
  }

  public boolean isRefunded() {
    return status == OrderStatus.REFUNDED;
  }

  private void setMemberId(@NonNull Long memberId) {
    this.memberId = memberId;
  }

  private void setTransactionId(@NonNull String transactionId) {
    this.transactionId = transactionId;
  }

  private void setPaymentMethod(@NonNull PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  private void setStatus(@NonNull OrderStatus status) {
    this.status = status;
  }

  private boolean isRefundable() {
    return status == OrderStatus.PAID;
  }
}
