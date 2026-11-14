package kr.co.deepple.payment.adapter.out.store;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.model.ResponseBodyV2DecodedPayload;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.domain.PaymentErrorCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppStoreClient {

  private final SignedDataVerifier signedDataVerifier;

  public JWSTransactionDecodedPayload verifyAndDecodeTransaction(
      @NonNull String signedTransaction) {
    try {
      return signedDataVerifier.verifyAndDecodeTransaction(signedTransaction);
    } catch (VerificationException exception) {
      throw new DeeppleException(
          PaymentErrorCode.INVALID_APP_STORE_RECEIPT, exception.getMessage(), exception);
    }
  }

  public ResponseBodyV2DecodedPayload verifyAndDecodeNotification(@NonNull String signedPayload) {
    try {
      return signedDataVerifier.verifyAndDecodeNotification(signedPayload);
    } catch (VerificationException exception) {
      throw new DeeppleException(
          PaymentErrorCode.INVALID_APP_STORE_RECEIPT, exception.getMessage(), exception);
    } catch (Exception exception) {
      throw new DeeppleException(
          PaymentErrorCode.APP_STORE_UNAVAILABLE, exception.getMessage(), exception);
    }
  }
}
