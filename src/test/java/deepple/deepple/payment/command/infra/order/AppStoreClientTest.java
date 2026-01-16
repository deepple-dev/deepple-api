package deepple.deepple.payment.command.infra.order;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.model.ResponseBodyV2DecodedPayload;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import deepple.deepple.payment.command.infra.order.exception.AppStoreClientException;
import deepple.deepple.payment.command.infra.order.exception.InvalidAppReceiptException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppStoreClient 단위 테스트")
class AppStoreClientTest {

    private static final String SIGNED_TRANSACTION = "signed.transaction.jws";
    private static final String SIGNED_PAYLOAD = "signed.payload.jws";

    @Mock
    private SignedDataVerifier signedDataVerifier;

    @Mock
    private JWSTransactionDecodedPayload decodedPayload;

    @Mock
    private ResponseBodyV2DecodedPayload notificationPayload;

    @InjectMocks
    private AppStoreClient appStoreClient;

    @Nested
    @DisplayName("verifyAndDecodeTransaction 메서드는")
    class VerifyAndDecodeTransactionTests {

        @DisplayName("정상적으로 JWS를 검증하고 디코딩된 페이로드를 반환한다")
        @Test
        void whenSuccessful_returnsDecodedPayload() throws VerificationException {
            // given
            when(signedDataVerifier.verifyAndDecodeTransaction(SIGNED_TRANSACTION)).thenReturn(decodedPayload);

            // when
            JWSTransactionDecodedPayload result = appStoreClient.verifyAndDecodeTransaction(SIGNED_TRANSACTION);

            // then
            assertThat(result).isEqualTo(decodedPayload);
        }

        @DisplayName("JWS 검증 실패 시 InvalidAppReceiptException을 던진다")
        @Test
        void whenVerificationFails_throwsInvalidAppReceiptException() throws VerificationException {
            // given
            VerificationException cause = mock(VerificationException.class);
            when(signedDataVerifier.verifyAndDecodeTransaction(SIGNED_TRANSACTION)).thenThrow(cause);

            // when & then
            assertThatThrownBy(() -> appStoreClient.verifyAndDecodeTransaction(SIGNED_TRANSACTION))
                .isInstanceOf(InvalidAppReceiptException.class)
                .hasCause(cause);
        }
    }

    @Nested
    @DisplayName("verifyAndDecodeNotification 메서드는")
    class VerifyAndDecodeNotificationTests {

        @DisplayName("정상적으로 알림 페이로드를 검증하고 디코딩된 페이로드를 반환한다")
        @Test
        void whenSuccessful_returnsDecodedPayload() throws VerificationException {
            // given
            when(signedDataVerifier.verifyAndDecodeNotification(SIGNED_PAYLOAD)).thenReturn(notificationPayload);

            // when
            ResponseBodyV2DecodedPayload result = appStoreClient.verifyAndDecodeNotification(SIGNED_PAYLOAD);

            // then
            assertThat(result).isEqualTo(notificationPayload);
        }

        @DisplayName("알림 검증 실패 시 InvalidAppReceiptException을 던진다")
        @Test
        void whenVerificationFails_throwsInvalidAppReceiptException() throws VerificationException {
            // given
            VerificationException cause = mock(VerificationException.class);
            when(signedDataVerifier.verifyAndDecodeNotification(SIGNED_PAYLOAD)).thenThrow(cause);

            // when & then
            assertThatThrownBy(() -> appStoreClient.verifyAndDecodeNotification(SIGNED_PAYLOAD))
                .isInstanceOf(InvalidAppReceiptException.class)
                .hasCause(cause);
        }

        @DisplayName("일반 예외 발생 시 AppStoreClientException을 던진다")
        @Test
        void whenGeneralExceptionOccurs_throwsAppStoreClientException() throws VerificationException {
            // given
            RuntimeException cause = new RuntimeException("General error");
            when(signedDataVerifier.verifyAndDecodeNotification(SIGNED_PAYLOAD)).thenThrow(cause);

            // when & then
            assertThatThrownBy(() -> appStoreClient.verifyAndDecodeNotification(SIGNED_PAYLOAD))
                .isInstanceOf(AppStoreClientException.class)
                .hasCause(cause);
        }
    }
}