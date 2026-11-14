package kr.co.deepple.payment.application;

import java.time.LocalDateTime;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.application.port.out.HeartPurchaseOptionRepository;
import kr.co.deepple.payment.application.port.out.OrderRepository;
import kr.co.deepple.payment.application.port.out.RefundRepository;
import kr.co.deepple.payment.domain.HeartPurchaseOption;
import kr.co.deepple.payment.domain.NotificationType;
import kr.co.deepple.payment.domain.Order;
import kr.co.deepple.payment.domain.PaymentErrorCode;
import kr.co.deepple.payment.domain.PaymentMethod;
import kr.co.deepple.payment.domain.Refund;
import kr.co.deepple.payment.domain.RefundDetail;
import kr.co.deepple.payment.event.HeartRefundedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

  private final OrderRepository orderRepository;
  private final RefundRepository refundRepository;
  private final HeartPurchaseOptionRepository heartPurchaseOptionRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void processRefund(
      String transactionId,
      String productId,
      int quantity,
      PaymentMethod paymentMethod,
      NotificationType notificationType) {
    Order order = findOrderForUpdate(transactionId, paymentMethod);
    if (order.isRefunded()) {
      log.atInfo().addKeyValue("transactionId", transactionId).log("이미 환불된 거래");
      return;
    }

    order.refund();
    Long refundAmount = calculateRefundAmount(productId, quantity);
    saveRefund(order, productId, quantity, refundAmount, notificationType);
    raiseRefundEvent(order, refundAmount);

    log.atInfo()
        .addKeyValue("transactionId", transactionId)
        .addKeyValue("memberId", order.getMemberId())
        .addKeyValue("amount", refundAmount)
        .log("환불 완료");
  }

  private Order findOrderForUpdate(String transactionId, PaymentMethod paymentMethod) {
    return orderRepository
        .findByTransactionIdAndPaymentMethodForUpdate(transactionId, paymentMethod)
        .orElseThrow(() ->
            new DeeppleException(
                PaymentErrorCode.ORDER_NOT_FOUND,
                "주문을 찾을 수 없습니다. transactionId: " + transactionId));
  }

  private Long calculateRefundAmount(String productId, int quantity) {
    HeartPurchaseOption heartPurchaseOption = findHeartPurchaseOption(productId);
    return heartPurchaseOption.getHeartAmount() * quantity;
  }

  private HeartPurchaseOption findHeartPurchaseOption(String productId) {
    return heartPurchaseOptionRepository
        .findByProductIdAndDeletedAtIsNull(productId)
        .orElseThrow(() ->
            new DeeppleException(
                PaymentErrorCode.HEART_PURCHASE_OPTION_NOT_FOUND,
                "하트 구매 옵션이 존재하지 않습니다. product id:" + productId));
  }

  private void saveRefund(
      Order order,
      String productId,
      int quantity,
      long refundAmount,
      NotificationType notificationType) {
    RefundDetail refundDetail = RefundDetail.of(productId, quantity, refundAmount);
    Refund refund =
        Refund.of(
            order.getId(),
            order.getMemberId(),
            order.getTransactionId(),
            refundDetail,
            order.getPaymentMethod(),
            notificationType,
            LocalDateTime.now());
    refundRepository.save(refund);
  }

  private void raiseRefundEvent(Order order, long refundAmount) {
    eventPublisher.publishEvent(
        HeartRefundedEvent.of(
            order.getMemberId(), order.getId(), refundAmount, order.getTransactionId()));
  }
}
