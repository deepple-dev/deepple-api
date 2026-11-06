package deepple.deepple.payment.command.domain.order.event;

import deepple.deepple.common.event.Event;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

@Getter
public class HeartRefundedEvent extends Event {
    private final Long memberId;
    private final Long amount;
    private final String transactionId;

    private HeartRefundedEvent(@NonNull Long memberId, @NonNull Long amount, @NonNull String transactionId) {
        this.memberId = memberId;
        this.amount = amount;
        this.transactionId = transactionId;
    }

    public static HeartRefundedEvent of(Long memberId, Long amount, String transactionId) {
        return new HeartRefundedEvent(memberId, amount, transactionId);
    }

    @Override
    public Map<String, Object> logAttributes() {
        return Map.of("memberId", memberId, "amount", amount, "transactionId", transactionId);
    }
}
