package kr.co.deepple.payment.application;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.lock.DistributedLockTemplate;
import kr.co.deepple.payment.application.port.out.HeartPurchaseOptionRepository;
import kr.co.deepple.payment.application.port.out.OrderRepository;
import kr.co.deepple.payment.domain.HeartPurchaseOption;
import kr.co.deepple.payment.domain.Order;
import kr.co.deepple.payment.domain.PaymentErrorCode;
import kr.co.deepple.payment.domain.PaymentMethod;
import kr.co.deepple.payment.event.HeartPurchasedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final HeartPurchaseOptionRepository heartPurchaseOptionRepository;
  private final DistributedLockTemplate distributedLockTemplate;
  private final ApplicationEventPublisher eventPublisher;

  public void processReceipt(
      long memberId,
      String transactionId,
      String productId,
      int quantity,
      PaymentMethod paymentMethod) {
    distributedLockTemplate.executeInTransaction(
        lockKey(transactionId, paymentMethod),
        () -> processReceiptInLock(memberId, transactionId, productId, quantity, paymentMethod));
  }

  private void processReceiptInLock(
      long memberId,
      String transactionId,
      String productId,
      int quantity,
      PaymentMethod paymentMethod) {
    Optional<Order> existingOrder =
        orderRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod);
    if (existingOrder.isPresent()) {
      verifyOrderedBy(existingOrder.get(), memberId);
      log.atInfo()
          .addKeyValue("transactionId", transactionId)
          .addKeyValue("paymentMethod", paymentMethod)
          .log("이미 처리된 결제");
      return;
    }

    Order order = orderRepository.save(Order.of(memberId, transactionId, paymentMethod));
    purchaseHeart(memberId, order.getId(), productId, quantity);
    log.atInfo()
        .addKeyValue("transactionId", transactionId)
        .addKeyValue("productId", productId)
        .addKeyValue("quantity", quantity)
        .addKeyValue("paymentMethod", paymentMethod)
        .log("결제 완료");
  }

  private String lockKey(String transactionId, PaymentMethod paymentMethod) {
    return "order:" + paymentMethod + ":" + transactionId;
  }

  private void verifyOrderedBy(Order order, long memberId) {
    if (!order.isOrderedBy(memberId)) {
      throw new DeeppleException(
          PaymentErrorCode.ORDER_ALREADY_EXISTS,
          "transactionId="
              + order.getTransactionId()
              + ", paymentMethod="
              + order.getPaymentMethod());
    }
  }

  private void purchaseHeart(long memberId, long orderId, String productId, int quantity) {
    String normalizedProductId = productId.toUpperCase();
    HeartPurchaseOption heartPurchaseOption =
        heartPurchaseOptionRepository
            .findByProductIdAndDeletedAtIsNull(normalizedProductId)
            .orElseThrow(() ->
                new DeeppleException(
                    PaymentErrorCode.HEART_PURCHASE_OPTION_NOT_FOUND,
                    "하트 구매 옵션이 존재하지 않습니다. product id:" + productId));
    eventPublisher.publishEvent(
        HeartPurchasedEvent.of(
            memberId, orderId, heartPurchaseOption.calculateHeartAmount(quantity)));
  }
}
