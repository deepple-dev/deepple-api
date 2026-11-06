package deepple.deepple.payment.command.domain.order.event;

import deepple.deepple.common.event.Event;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

@Getter
public class HeartPurchasedEvent extends Event {
    private final Long memberId;
    private final Long amount;

    private HeartPurchasedEvent(@NonNull Long memberId, @NonNull Long amount) {
        this.memberId = memberId;
        this.amount = amount;
    }

    public static HeartPurchasedEvent of(Long memberId, Long amount) {
        return new HeartPurchasedEvent(memberId, amount);
    }

    @Override
    public Map<String, Object> logAttributes() {
        return Map.of("memberId", memberId, "amount", amount);
    }
}
