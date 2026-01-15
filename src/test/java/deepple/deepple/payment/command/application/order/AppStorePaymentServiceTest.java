package deepple.deepple.payment.command.application.order;

import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import deepple.deepple.common.event.Events;
import deepple.deepple.payment.command.application.order.exception.InvalidOrderException;
import deepple.deepple.payment.command.domain.order.event.AppStoreReceiptVerifiedEvent;
import deepple.deepple.payment.command.infra.order.AppStoreClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppStorePaymentServiceTest {

    @Mock
    private AppStoreClient appStoreClient;

    @InjectMocks
    private AppStorePaymentService appStorePaymentService;

    @Test
    @DisplayName("signedTransaction이 검증되고, transactionInfo가 paid 상태이며, transactionId가 처리된 기록이 없는 경우 주문을 생성하고 하트 구매 옵션을 구매한다.")
    void successWhenSignedTransactionIsVerifiedAndTransactionInfoIsPaidAndTransactionIdIsNotExists() {
        // Given
        String signedTransaction = "signed.transaction.jws";
        long memberId = 1L;
        String transactionId = "transactionId";
        String productId = "productId";
        int quantity = 1;

        JWSTransactionDecodedPayload decodedPayload = mock(JWSTransactionDecodedPayload.class);
        when(decodedPayload.getRevocationDate()).thenReturn(null);
        when(decodedPayload.getTransactionId()).thenReturn(transactionId);
        when(decodedPayload.getProductId()).thenReturn(productId);
        when(decodedPayload.getQuantity()).thenReturn(quantity);

        when(appStoreClient.verifyAndDecodeTransaction(signedTransaction)).thenReturn(decodedPayload);

        // When & Then
        try (MockedStatic<Events> eventsMockedStatic = mockStatic(Events.class)) {
            appStorePaymentService.verifyReceipt(signedTransaction, memberId);
            eventsMockedStatic.verify(
                () -> Events.raise(argThat(
                        event -> event instanceof AppStoreReceiptVerifiedEvent
                            && ((AppStoreReceiptVerifiedEvent) event).getMemberId() == memberId
                            && ((AppStoreReceiptVerifiedEvent) event).getTransactionId().equals(transactionId)
                            && ((AppStoreReceiptVerifiedEvent) event).getProductId().equals(productId)
                            && ((AppStoreReceiptVerifiedEvent) event).getQuantity() == quantity
                    )
                )
            );
        }
    }

    @Test
    @DisplayName("signedTransaction이 검증되고, transactionInfo가 revoked 상태인 경우 InvalidOrderException을 발생시킨다.")
    void throwInvalidOrderExceptionWhenTransactionInfoIsRevoked() {
        // Given
        String signedTransaction = "signed.transaction.jws";
        Long memberId = 1L;

        JWSTransactionDecodedPayload decodedPayload = mock(JWSTransactionDecodedPayload.class);
        Long revocationDate = 123456789L;
        when(decodedPayload.getRevocationDate()).thenReturn(revocationDate);

        when(appStoreClient.verifyAndDecodeTransaction(signedTransaction)).thenReturn(decodedPayload);

        // When & Then
        assertThatThrownBy(() ->
            appStorePaymentService.verifyReceipt(signedTransaction, memberId))
            .isInstanceOf(InvalidOrderException.class);
    }
}
