package deepple.deepple.payment.command.domain.refund;

import deepple.deepple.common.entity.BaseEntity;
import deepple.deepple.payment.command.domain.order.Order;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds",
    indexes = {
        @Index(name = "idx_refunds_member_id", columnList = "memberId")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_refunds_transaction_id", columnNames = "transactionId")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String transactionId;

    @Embedded
    private RefundDetail refundDetail;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)", nullable = false)
    private NotificationType notificationType;

    @Column(nullable = false)
    private LocalDateTime refundedAt;

    private Refund(
        @NonNull Order order,
        @NonNull RefundDetail refundDetail,
        @NonNull NotificationType notificationType
    ) {
        this.order = order;
        this.memberId = order.getMemberId();
        this.transactionId = order.getTransactionId();
        this.refundDetail = refundDetail;
        this.paymentMethod = order.getPaymentMethod();
        this.notificationType = notificationType;
        this.refundedAt = LocalDateTime.now();
    }

    public static Refund of(Order order, RefundDetail refundDetail, NotificationType notificationType) {
        return new Refund(order, refundDetail, notificationType);
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
