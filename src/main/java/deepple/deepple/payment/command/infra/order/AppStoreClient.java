package deepple.deepple.payment.command.infra.order;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.model.ResponseBodyV2DecodedPayload;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import deepple.deepple.payment.command.infra.order.exception.AppStoreClientException;
import deepple.deepple.payment.command.infra.order.exception.InvalidAppReceiptException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppStoreClient {

    private final SignedDataVerifier signedDataVerifier;

    public JWSTransactionDecodedPayload verifyAndDecodeTransaction(@NonNull String signedTransaction) {
        try {
            return signedDataVerifier.verifyAndDecodeTransaction(signedTransaction);
        } catch (VerificationException exception) {
            throw new InvalidAppReceiptException(exception);
        }
    }

    public ResponseBodyV2DecodedPayload verifyAndDecodeNotification(@NonNull String signedPayload) {
        try {
            return signedDataVerifier.verifyAndDecodeNotification(signedPayload);
        } catch (VerificationException exception) {
            throw new InvalidAppReceiptException(exception);
        } catch (Exception exception) {
            throw new AppStoreClientException(exception);
        }
    }
}
