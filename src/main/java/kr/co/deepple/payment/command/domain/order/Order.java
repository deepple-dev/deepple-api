package kr.co.deepple.payment.command.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.command.domain.PaymentErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long memberId;

  private String transactionId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private OrderStatus status;

  private Order(
      long memberId, @NonNull String transactionId, @NonNull PaymentMethod paymentMethod) {
    this.memberId = memberId;
    this.transactionId = transactionId;
    this.paymentMethod = paymentMethod;
    this.status = OrderStatus.PAID;
  }

  public static Order of(long memberId, String transactionId, PaymentMethod paymentMethod) {
    return new Order(memberId, transactionId, paymentMethod);
  }

  public void refund() {
    if (!isRefundable()) {
      throw new DeeppleException(
          PaymentErrorCode.ORDER_NOT_REFUNDABLE, "PAID 상태의 주문만 환불할 수 있습니다. orderStatus=" + status);
    }
    this.status = OrderStatus.REFUNDED;
  }

  public boolean isOrderedBy(long memberId) {
    return this.memberId == memberId;
  }

  public boolean isRefunded() {
    return status == OrderStatus.REFUNDED;
  }

  private boolean isRefundable() {
    return status == OrderStatus.PAID;
  }
}
