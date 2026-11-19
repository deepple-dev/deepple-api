package kr.co.deepple.heart.adapter.in.kafka;

import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.heart.application.port.in.AddPurchaseHeartCommand;
import kr.co.deepple.heart.application.port.in.AddPurchaseHeartUseCase;
import kr.co.deepple.heart.application.port.in.RefundHeartCommand;
import kr.co.deepple.heart.application.port.in.RefundHeartUseCase;
import kr.co.deepple.payment.event.HeartPurchasedEvent;
import kr.co.deepple.payment.event.HeartRefundedEvent;
import kr.co.deepple.payment.event.PaymentTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("heartPaymentEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = PaymentTopic.NAME, groupId = HeartConsumerGroup.ID)
public class PaymentEventConsumer extends EventConsumer {

  private final AddPurchaseHeartUseCase addPurchaseHeartUseCase;
  private final RefundHeartUseCase refundHeartUseCase;

  @KafkaHandler
  public void handle(HeartPurchasedEvent event) {
    addPurchaseHeartUseCase.addPurchaseHeart(
        new AddPurchaseHeartCommand(event.memberId(), event.orderId(), event.amount()));
  }

  @KafkaHandler
  public void handle(HeartRefundedEvent event) {
    refundHeartUseCase.refundHeart(
        new RefundHeartCommand(event.memberId(), event.orderId(), event.amount()));
  }
}
