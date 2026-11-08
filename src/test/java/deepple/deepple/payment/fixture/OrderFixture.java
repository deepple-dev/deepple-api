package deepple.deepple.payment.fixture;

import deepple.deepple.payment.command.domain.order.Order;
import deepple.deepple.payment.command.domain.order.OrderStatus;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderFixture {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private Long id;
    private long memberId = 1L;
    private String transactionId;
    private PaymentMethod paymentMethod = PaymentMethod.GOOGLE_PLAY;
    private OrderStatus status = OrderStatus.PAID;

    public static OrderFixture builder() {
        return new OrderFixture();
    }

    public Order build() {
        String resolvedTransactionId =
            transactionId == null ? "transaction-" + SEQUENCE.incrementAndGet() : transactionId;
        Order order = Order.of(memberId, resolvedTransactionId, paymentMethod);
        setField(order, "id", id);
        setField(order, "status", status);
        return order;
    }
}
