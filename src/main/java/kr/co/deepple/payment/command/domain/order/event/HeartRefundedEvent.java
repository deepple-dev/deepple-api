package kr.co.deepple.payment.command.domain.order.event;

import kr.co.deepple.common.event.Event;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

@Getter
public class HeartRefundedEvent extends Event {
    private final long memberId;
    private final long orderId;
    private final long amount;
    private final String transactionId;

    private HeartRefundedEvent(long memberId, long orderId, long amount, @NonNull String transactionId) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.amount = amount;
        this.transactionId = transactionId;
    }

    public static HeartRefundedEvent of(long memberId, long orderId, long amount, String transactionId) {
        return new HeartRefundedEvent(memberId, orderId, amount, transactionId);
    }

    @Override
    public Map<String, Object> logAttributes() {
        return Map.of("memberId", memberId, "orderId", orderId, "amount", amount, "transactionId", transactionId);
    }
}
