package deepple.deepple.payment.command.application.order;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.model.NotificationTypeV2;
import com.apple.itunes.storekit.model.ResponseBodyV2DecodedPayload;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import deepple.deepple.payment.command.domain.refund.NotificationType;
import deepple.deepple.payment.command.infra.order.AppStoreClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppStoreNotificationServiceTest {

    @Mock
    private AppStoreClient appStoreClient;

    @Mock
    private RefundService refundService;

    @InjectMocks
    private AppStoreNotificationService appStoreNotificationService;

    @Nested
    @DisplayName("알림 처리 시")
    class HandleNotificationTests {

        @Test
        @DisplayName("REFUND 알림 수신 시 환불 처리")
        void shouldProcessRefundWhenRefundNotificationReceived() {
            // given
            String signedPayload = "signed.payload.data";
            String transactionId = "txn123";
            String productId = "product123";
            Integer quantity = 2;
            String signedTransactionInfo = "signed.transaction.info";

            ResponseBodyV2DecodedPayload payload = mock(ResponseBodyV2DecodedPayload.class, RETURNS_DEEP_STUBS);
            NotificationTypeV2 notificationTypeV2 = mock(NotificationTypeV2.class);
            JWSTransactionDecodedPayload transaction = mock(JWSTransactionDecodedPayload.class);

            when(appStoreClient.verifyAndDecodeNotification(signedPayload)).thenReturn(payload);
            when(payload.getNotificationType()).thenReturn(notificationTypeV2);
            when(notificationTypeV2.getValue()).thenReturn("REFUND");
            when(payload.getData().getSignedTransactionInfo()).thenReturn(signedTransactionInfo);
            when(appStoreClient.verifyAndDecodeTransaction(signedTransactionInfo)).thenReturn(transaction);
            when(transaction.getTransactionId()).thenReturn(transactionId);
            when(transaction.getProductId()).thenReturn(productId);
            when(transaction.getQuantity()).thenReturn(quantity);

            // when
            appStoreNotificationService.handleNotification(signedPayload);

            // then
            verify(appStoreClient).verifyAndDecodeNotification(signedPayload);
            verify(appStoreClient).verifyAndDecodeTransaction(signedTransactionInfo);
            verify(refundService).processRefund(
                eq(transactionId),
                eq(productId),
                eq(quantity),
                eq(PaymentMethod.APP_STORE),
                eq(NotificationType.REFUND)
            );
        }

        @Test
        @DisplayName("REFUND가 아닌 알림 수신 시 환불 처리하지 않음")
        void shouldNotProcessRefundWhenNonRefundNotificationReceived() {
            // given
            String signedPayload = "signed.payload.data";

            ResponseBodyV2DecodedPayload payload = mock(ResponseBodyV2DecodedPayload.class);
            NotificationTypeV2 notificationTypeV2 = mock(NotificationTypeV2.class);

            when(appStoreClient.verifyAndDecodeNotification(signedPayload)).thenReturn(payload);
            when(payload.getNotificationType()).thenReturn(notificationTypeV2);
            when(notificationTypeV2.getValue()).thenReturn("DID_RENEW");

            // when
            appStoreNotificationService.handleNotification(signedPayload);

            // then
            verify(appStoreClient).verifyAndDecodeNotification(signedPayload);
            verify(refundService, never()).processRefund(any(), any(), anyInt(), any(), any());
        }

        @Test
        @DisplayName("CONSUMPTION_REQUEST 알림 수신 시 환불 처리하지 않음")
        void shouldNotProcessRefundWhenConsumptionRequestNotificationReceived() {
            // given
            String signedPayload = "signed.payload.data";

            ResponseBodyV2DecodedPayload payload = mock(ResponseBodyV2DecodedPayload.class);
            NotificationTypeV2 notificationTypeV2 = mock(NotificationTypeV2.class);

            when(appStoreClient.verifyAndDecodeNotification(signedPayload)).thenReturn(payload);
            when(payload.getNotificationType()).thenReturn(notificationTypeV2);
            when(notificationTypeV2.getValue()).thenReturn("CONSUMPTION_REQUEST");

            // when
            appStoreNotificationService.handleNotification(signedPayload);

            // then
            verify(appStoreClient).verifyAndDecodeNotification(signedPayload);
            verify(refundService, never()).processRefund(any(), any(), anyInt(), any(), any());
        }

        @Test
        @DisplayName("UNKNOWN 알림 수신 시 환불 처리하지 않음")
        void shouldNotProcessRefundWhenUnknownNotificationReceived() {
            // given
            String signedPayload = "signed.payload.data";

            ResponseBodyV2DecodedPayload payload = mock(ResponseBodyV2DecodedPayload.class);
            NotificationTypeV2 notificationTypeV2 = mock(NotificationTypeV2.class);

            when(appStoreClient.verifyAndDecodeNotification(signedPayload)).thenReturn(payload);
            when(payload.getNotificationType()).thenReturn(notificationTypeV2);
            when(notificationTypeV2.getValue()).thenReturn("UNKNOWN_TYPE");

            // when
            appStoreNotificationService.handleNotification(signedPayload);

            // then
            verify(appStoreClient).verifyAndDecodeNotification(signedPayload);
            verify(refundService, never()).processRefund(any(), any(), anyInt(), any(), any());
        }
    }

    @Nested
    @DisplayName("환불 알림 처리 시")
    class HandleRefundNotificationTests {

        @Test
        @DisplayName("정상적인 REFUND 알림 처리")
        void shouldHandleRefundNotificationSuccessfully() {
            // given
            String signedPayload = "signed.payload.data";
            String transactionId = "txn456";
            String productId = "product456";
            Integer quantity = 1;
            String signedTransactionInfo = "signed.transaction.info";

            ResponseBodyV2DecodedPayload payload = mock(ResponseBodyV2DecodedPayload.class, RETURNS_DEEP_STUBS);
            NotificationTypeV2 notificationTypeV2 = mock(NotificationTypeV2.class);
            JWSTransactionDecodedPayload transaction = mock(JWSTransactionDecodedPayload.class);

            when(appStoreClient.verifyAndDecodeNotification(signedPayload)).thenReturn(payload);
            when(payload.getNotificationType()).thenReturn(notificationTypeV2);
            when(notificationTypeV2.getValue()).thenReturn("REFUND");
            when(payload.getData().getSignedTransactionInfo()).thenReturn(signedTransactionInfo);
            when(appStoreClient.verifyAndDecodeTransaction(signedTransactionInfo)).thenReturn(transaction);
            when(transaction.getTransactionId()).thenReturn(transactionId);
            when(transaction.getProductId()).thenReturn(productId);
            when(transaction.getQuantity()).thenReturn(quantity);

            // when
            appStoreNotificationService.handleNotification(signedPayload);

            // then
            verify(appStoreClient).verifyAndDecodeNotification(signedPayload);
            verify(appStoreClient).verifyAndDecodeTransaction(signedTransactionInfo);
            verify(refundService).processRefund(transactionId, productId, quantity, PaymentMethod.APP_STORE,
                NotificationType.REFUND);
        }

        @Test
        @DisplayName("quantity가 여러 개인 REFUND 알림 처리")
        void shouldHandleRefundNotificationWithMultipleQuantity() {
            // given
            String signedPayload = "signed.payload.data";
            String transactionId = "txn789";
            String productId = "product789";
            Integer quantity = 5;
            String signedTransactionInfo = "signed.transaction.info";

            ResponseBodyV2DecodedPayload payload = mock(ResponseBodyV2DecodedPayload.class, RETURNS_DEEP_STUBS);
            NotificationTypeV2 notificationTypeV2 = mock(NotificationTypeV2.class);
            JWSTransactionDecodedPayload transaction = mock(JWSTransactionDecodedPayload.class);

            when(appStoreClient.verifyAndDecodeNotification(signedPayload)).thenReturn(payload);
            when(payload.getNotificationType()).thenReturn(notificationTypeV2);
            when(notificationTypeV2.getValue()).thenReturn("REFUND");
            when(payload.getData().getSignedTransactionInfo()).thenReturn(signedTransactionInfo);
            when(appStoreClient.verifyAndDecodeTransaction(signedTransactionInfo)).thenReturn(transaction);
            when(transaction.getTransactionId()).thenReturn(transactionId);
            when(transaction.getProductId()).thenReturn(productId);
            when(transaction.getQuantity()).thenReturn(quantity);

            // when
            appStoreNotificationService.handleNotification(signedPayload);

            // then
            verify(refundService).processRefund(transactionId, productId, 5, PaymentMethod.APP_STORE,
                NotificationType.REFUND);
        }
    }

    @Nested
    @DisplayName("알림 검증 실패 시")
    class NotificationVerificationFailureTests {

        @Test
        @DisplayName("AppStoreClient에서 예외 발생 시 예외 전파")
        void shouldPropagateExceptionWhenAppStoreClientThrowsException() {
            // given
            String signedPayload = "invalid.payload";
            RuntimeException exception = new RuntimeException("Verification failed");

            when(appStoreClient.verifyAndDecodeNotification(signedPayload)).thenThrow(exception);

            // when & then
            assertThatThrownBy(() -> appStoreNotificationService.handleNotification(signedPayload))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Verification failed");

            verify(refundService, never()).processRefund(any(), any(), anyInt(), any(), any());
        }
    }

    @Nested
    @DisplayName("환불 처리 중 예외 발생 시")
    class RefundProcessingExceptionTests {

        @Test
        @DisplayName("RefundService에서 예외 발생 시 예외 전파")
        void shouldPropagateExceptionWhenRefundServiceThrowsException() {
            // given
            String signedPayload = "signed.payload.data";
            String transactionId = "txn123";
            String productId = "product123";
            Integer quantity = 1;
            String signedTransactionInfo = "signed.transaction.info";

            ResponseBodyV2DecodedPayload payload = mock(ResponseBodyV2DecodedPayload.class, RETURNS_DEEP_STUBS);
            NotificationTypeV2 notificationTypeV2 = mock(NotificationTypeV2.class);
            JWSTransactionDecodedPayload transaction = mock(JWSTransactionDecodedPayload.class);

            when(appStoreClient.verifyAndDecodeNotification(signedPayload)).thenReturn(payload);
            when(payload.getNotificationType()).thenReturn(notificationTypeV2);
            when(notificationTypeV2.getValue()).thenReturn("REFUND");
            when(payload.getData().getSignedTransactionInfo()).thenReturn(signedTransactionInfo);
            when(appStoreClient.verifyAndDecodeTransaction(signedTransactionInfo)).thenReturn(transaction);
            when(transaction.getTransactionId()).thenReturn(transactionId);
            when(transaction.getProductId()).thenReturn(productId);
            when(transaction.getQuantity()).thenReturn(quantity);

            RuntimeException exception = new RuntimeException("Refund processing failed");
            doThrow(exception).when(refundService).processRefund(any(), any(), anyInt(), any(), any());

            // when & then
            assertThatThrownBy(() -> appStoreNotificationService.handleNotification(signedPayload))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Refund processing failed");
        }
    }

    @Nested
    @DisplayName("PaymentMethod 처리 테스트")
    class PaymentMethodHandlingTests {

        @Test
        @DisplayName("환불 알림 처리 시 PaymentMethod를 APP_STORE로 설정")
        void shouldUseAppStorePaymentMethodForRefund() {
            // given
            String signedPayload = "signed.payload.data";
            String transactionId = "txn123";
            String productId = "product123";
            Integer quantity = 1;
            String signedTransactionInfo = "signed.transaction.info";

            ResponseBodyV2DecodedPayload payload = mock(ResponseBodyV2DecodedPayload.class, RETURNS_DEEP_STUBS);
            NotificationTypeV2 notificationTypeV2 = mock(NotificationTypeV2.class);
            JWSTransactionDecodedPayload transaction = mock(JWSTransactionDecodedPayload.class);

            when(appStoreClient.verifyAndDecodeNotification(signedPayload)).thenReturn(payload);
            when(payload.getNotificationType()).thenReturn(notificationTypeV2);
            when(notificationTypeV2.getValue()).thenReturn("REFUND");
            when(payload.getData().getSignedTransactionInfo()).thenReturn(signedTransactionInfo);
            when(appStoreClient.verifyAndDecodeTransaction(signedTransactionInfo)).thenReturn(transaction);
            when(transaction.getTransactionId()).thenReturn(transactionId);
            when(transaction.getProductId()).thenReturn(productId);
            when(transaction.getQuantity()).thenReturn(quantity);

            // when
            appStoreNotificationService.handleNotification(signedPayload);

            // then
            verify(refundService).processRefund(
                any(),
                any(),
                anyInt(),
                eq(PaymentMethod.APP_STORE),
                any()
            );
        }
    }
}
