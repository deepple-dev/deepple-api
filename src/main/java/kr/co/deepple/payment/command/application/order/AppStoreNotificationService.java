package kr.co.deepple.payment.command.application.order;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.model.ResponseBodyV2DecodedPayload;
import kr.co.deepple.payment.command.domain.order.PaymentMethod;
import kr.co.deepple.payment.command.domain.refund.NotificationType;
import kr.co.deepple.payment.command.infra.order.AppStoreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppStoreNotificationService {

  private final AppStoreClient appStoreClient;
  private final RefundService refundService;

  public void handleNotification(String signedPayload) {
    ResponseBodyV2DecodedPayload payload = verifyNotification(signedPayload);
    try {
      processNotification(payload);
    } catch (Exception e) {
      log.atError()
          .setCause(e)
          .addKeyValue("notificationUUID", payload.getNotificationUUID())
          .addKeyValue("notificationType", payload.getRawNotificationType())
          .log("Apple 알림 처리 실패");
    }
  }

  private void processNotification(ResponseBodyV2DecodedPayload payload) {
    NotificationType notificationType =
        NotificationType.from(payload.getNotificationType().getValue());

    if (notificationType.isRefund()) {
      handleRefund(payload, notificationType);
    } else {
      log.atInfo().addKeyValue("notificationType", notificationType).log("처리하지 않는 Apple 알림");
    }
  }

  private ResponseBodyV2DecodedPayload verifyNotification(String signedPayload) {
    return appStoreClient.verifyAndDecodeNotification(signedPayload);
  }

  private void handleRefund(
      ResponseBodyV2DecodedPayload payload, NotificationType notificationType) {
    JWSTransactionDecodedPayload transaction = extractTransaction(payload);
    processRefundTransaction(transaction, notificationType);
  }

  private JWSTransactionDecodedPayload extractTransaction(ResponseBodyV2DecodedPayload payload) {
    String signedTransactionInfo = payload.getData().getSignedTransactionInfo();
    return appStoreClient.verifyAndDecodeTransaction(signedTransactionInfo);
  }

  private void processRefundTransaction(
      JWSTransactionDecodedPayload transaction, NotificationType notificationType) {
    String transactionId = transaction.getTransactionId();
    String productId = transaction.getProductId();
    Integer quantity = transaction.getQuantity();

    refundService.processRefund(
        transactionId, productId, quantity, PaymentMethod.APP_STORE, notificationType);
  }
}
