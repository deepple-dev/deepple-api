package deepple.deepple.payment.command.application.order;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.model.ResponseBodyV2DecodedPayload;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import deepple.deepple.payment.command.domain.refund.NotificationType;
import deepple.deepple.payment.command.infra.order.AppStoreClient;
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
        NotificationType notificationType = NotificationType.from(payload.getNotificationType().getValue());

        if (notificationType.isRefund()) {
            handleRefund(payload, notificationType);
        } else {
            log.info("처리하지 않는 알림 타입. notificationType: {}", notificationType);
        }
    }

    private ResponseBodyV2DecodedPayload verifyNotification(String signedPayload) {
        return appStoreClient.verifyAndDecodeNotification(signedPayload);
    }

    private void handleRefund(ResponseBodyV2DecodedPayload payload, NotificationType notificationType) {
        try {
            JWSTransactionDecodedPayload transaction = extractTransaction(payload);
            processRefundTransaction(transaction, notificationType);
        } catch (Exception e) {
            log.error("환불 알림 처리 실패", e);
            throw e;
        }
    }

    private JWSTransactionDecodedPayload extractTransaction(ResponseBodyV2DecodedPayload payload) {
        String signedTransactionInfo = payload.getData().getSignedTransactionInfo();
        return appStoreClient.verifyAndDecodeTransaction(signedTransactionInfo);
    }

    private void processRefundTransaction(JWSTransactionDecodedPayload transaction, NotificationType notificationType) {
        String transactionId = transaction.getTransactionId();
        String productId = transaction.getProductId();
        Integer quantity = transaction.getQuantity();

        refundService.processRefund(transactionId, productId, quantity, PaymentMethod.APP_STORE, notificationType);
    }
}
