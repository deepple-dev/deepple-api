package kr.co.deepple.payment.command.domain.order.event;

import java.util.Map;
import kr.co.deepple.common.event.Event;
import lombok.Getter;

@Getter
public class HeartPurchasedEvent extends Event {

  private final long memberId;
  private final long orderId;
  private final long amount;

  private HeartPurchasedEvent(long memberId, long orderId, long amount) {
    this.memberId = memberId;
    this.orderId = orderId;
    this.amount = amount;
  }

  public static HeartPurchasedEvent of(long memberId, long orderId, long amount) {
    return new HeartPurchasedEvent(memberId, orderId, amount);
  }

  @Override
  public Map<String, Object> logAttributes() {
    return Map.of("memberId", memberId, "orderId", orderId, "amount", amount);
  }
}
