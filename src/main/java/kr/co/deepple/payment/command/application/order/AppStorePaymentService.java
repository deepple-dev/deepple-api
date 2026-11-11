package kr.co.deepple.payment.command.application.order;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import kr.co.deepple.common.event.Events;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.command.domain.PaymentErrorCode;
import kr.co.deepple.payment.command.domain.order.event.AppStoreReceiptVerifiedEvent;
import kr.co.deepple.payment.command.infra.order.AppStoreClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppStorePaymentService {
    private final AppStoreClient appStoreClient;

    public void verifyReceipt(String signedTransaction, Long memberId) {
        JWSTransactionDecodedPayload decodedPayload = appStoreClient.verifyAndDecodeTransaction(signedTransaction);
        verifyPayload(decodedPayload);
        raiseReceiptVerifiedEvent(memberId, decodedPayload);
    }

    private void verifyPayload(JWSTransactionDecodedPayload decodedPayload) {
        if (decodedPayload.getRevocationDate() != null) {
            throw new DeeppleException(
                PaymentErrorCode.REVOKED_TRANSACTION,
                "transactionId=" + decodedPayload.getTransactionId()
            );
        }
    }

    private void raiseReceiptVerifiedEvent(final Long memberId,
        final JWSTransactionDecodedPayload decodedPayload) {
        Events.raise(
            AppStoreReceiptVerifiedEvent.of(
                memberId,
                decodedPayload.getTransactionId(),
                decodedPayload.getProductId(),
                decodedPayload.getQuantity()
            )
        );
    }
}
