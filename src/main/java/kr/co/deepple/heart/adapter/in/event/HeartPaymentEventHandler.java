package kr.co.deepple.heart.adapter.in.event;

import kr.co.deepple.heart.application.port.in.AddPurchaseHeartCommand;
import kr.co.deepple.heart.application.port.in.AddPurchaseHeartUseCase;
import kr.co.deepple.heart.application.port.in.RefundHeartCommand;
import kr.co.deepple.heart.application.port.in.RefundHeartUseCase;
import kr.co.deepple.payment.event.HeartPurchasedEvent;
import kr.co.deepple.payment.event.HeartRefundedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class HeartPaymentEventHandler {

  private final AddPurchaseHeartUseCase addPurchaseHeartUseCase;
  private final RefundHeartUseCase refundHeartUseCase;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(HeartPurchasedEvent event) {
    addPurchaseHeartUseCase.addPurchaseHeart(
        new AddPurchaseHeartCommand(event.memberId(), event.orderId(), event.amount()));
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(HeartRefundedEvent event) {
    refundHeartUseCase.refundHeart(
        new RefundHeartCommand(event.memberId(), event.orderId(), event.amount()));
  }
}
