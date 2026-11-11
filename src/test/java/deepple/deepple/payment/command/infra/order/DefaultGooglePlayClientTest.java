package deepple.deepple.payment.command.infra.order;

import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Purchases;
import com.google.api.services.androidpublisher.AndroidPublisher.Purchases.Products;
import com.google.api.services.androidpublisher.AndroidPublisher.Purchases.Products.Consume;
import com.google.api.services.androidpublisher.AndroidPublisher.Purchases.Products.Get;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.payment.command.domain.PaymentErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultGooglePlayClient 단위 테스트")
class DefaultGooglePlayClientTest {

    private static final String PACKAGE_NAME = "com.deepple.app";
    private static final String PRODUCT_ID = "heart_pack_100";
    private static final String PURCHASE_TOKEN = "purchase.token.value";
    private static final String ORDER_ID = "GPA.1234-5678-9012-34567";

    @Mock
    private AndroidPublisher androidPublisher;

    @Mock
    private Purchases purchases;

    @Mock
    private Products products;

    @Mock
    private Get getRequest;

    @Mock
    private Consume consumeRequest;

    private GooglePlayClient googlePlayClient;

    @BeforeEach
    void setUp() {
        googlePlayClient = new DefaultGooglePlayClient(androidPublisher);
        ReflectionTestUtils.setField(googlePlayClient, "packageName", PACKAGE_NAME);
    }

    private ProductPurchase purchasedAndNotConsumed() {
        return new ProductPurchase()
            .setOrderId(ORDER_ID)
            .setPurchaseState(0)
            .setConsumptionState(0)
            .setQuantity(1);
    }

    private void givenGetReturns(ProductPurchase purchase) throws IOException {
        when(androidPublisher.purchases()).thenReturn(purchases);
        when(purchases.products()).thenReturn(products);
        when(products.get(PACKAGE_NAME, PRODUCT_ID, PURCHASE_TOKEN)).thenReturn(getRequest);
        when(getRequest.execute()).thenReturn(purchase);
    }

    @Nested
    @DisplayName("verifyPurchase 메서드는")
    class VerifyPurchaseTests {

        @DisplayName("구매 상태가 정상(0)이고 소비되지 않았을 때 ProductPurchase를 반환한다")
        @Test
        void whenSuccessful_returnsProductPurchase() throws IOException {
            // given
            ProductPurchase purchase = purchasedAndNotConsumed();
            givenGetReturns(purchase);

            // when
            ProductPurchase result = googlePlayClient.verifyPurchase(PRODUCT_ID, PURCHASE_TOKEN);

            // then
            assertThat(result).isEqualTo(purchase);
        }

        @DisplayName("purchaseState가 0이 아니면 INVALID_GOOGLE_PLAY_RECEIPT 예외를 던진다")
        @Test
        void whenPurchaseStateNotPurchased_throwsInvalidException() throws IOException {
            // given
            ProductPurchase purchase = new ProductPurchase().setPurchaseState(2).setConsumptionState(0);
            givenGetReturns(purchase);

            // when & then
            assertThatThrownBy(() -> googlePlayClient.verifyPurchase(PRODUCT_ID, PURCHASE_TOKEN))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.INVALID_GOOGLE_PLAY_RECEIPT);
        }

        @DisplayName("purchaseState가 null이면 INVALID_GOOGLE_PLAY_RECEIPT 예외를 던진다")
        @Test
        void whenPurchaseStateNull_throwsInvalidException() throws IOException {
            // given
            ProductPurchase purchase = new ProductPurchase().setConsumptionState(0);
            givenGetReturns(purchase);

            // when & then
            assertThatThrownBy(() -> googlePlayClient.verifyPurchase(PRODUCT_ID, PURCHASE_TOKEN))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.INVALID_GOOGLE_PLAY_RECEIPT);
        }

        @DisplayName("consumptionState가 이미 소비됨(1)이어도 구매 정보를 돌려준다")
        @Test
        void whenAlreadyConsumed_returnsPurchase() throws IOException {
            // given
            ProductPurchase purchase = new ProductPurchase().setPurchaseState(0).setConsumptionState(1);
            givenGetReturns(purchase);

            // when & then
            assertThat(googlePlayClient.verifyPurchase(PRODUCT_ID, PURCHASE_TOKEN)).isSameAs(purchase);
        }

        @DisplayName("Google API 통신 실패 시 GOOGLE_PLAY_UNAVAILABLE 예외를 던진다")
        @Test
        void whenApiFails_throwsGooglePlayUnavailable() throws IOException {
            // given
            IOException cause = new IOException("network error");
            when(androidPublisher.purchases()).thenReturn(purchases);
            when(purchases.products()).thenReturn(products);
            when(products.get(PACKAGE_NAME, PRODUCT_ID, PURCHASE_TOKEN)).thenReturn(getRequest);
            when(getRequest.execute()).thenThrow(cause);

            // when & then
            assertThatThrownBy(() -> googlePlayClient.verifyPurchase(PRODUCT_ID, PURCHASE_TOKEN))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.GOOGLE_PLAY_UNAVAILABLE)
                .hasCause(cause);
        }
    }

    @Nested
    @DisplayName("consumePurchase 메서드는")
    class ConsumePurchaseTests {

        @DisplayName("정상적으로 소비 API를 호출한다")
        @Test
        void whenSuccessful_callsConsume() throws IOException {
            // given
            when(androidPublisher.purchases()).thenReturn(purchases);
            when(purchases.products()).thenReturn(products);
            when(products.consume(PACKAGE_NAME, PRODUCT_ID, PURCHASE_TOKEN)).thenReturn(consumeRequest);

            // when
            googlePlayClient.consumePurchase(PRODUCT_ID, PURCHASE_TOKEN);

            // then
            verify(consumeRequest).execute();
        }

        @DisplayName("Google API 통신 실패 시 GOOGLE_PLAY_UNAVAILABLE 예외를 던진다")
        @Test
        void whenApiFails_throwsGooglePlayUnavailable() throws IOException {
            // given
            IOException cause = new IOException("network error");
            when(androidPublisher.purchases()).thenReturn(purchases);
            when(purchases.products()).thenReturn(products);
            when(products.consume(PACKAGE_NAME, PRODUCT_ID, PURCHASE_TOKEN)).thenReturn(consumeRequest);
            when(consumeRequest.execute()).thenThrow(cause);

            // when & then
            assertThatThrownBy(() -> googlePlayClient.consumePurchase(PRODUCT_ID, PURCHASE_TOKEN))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.GOOGLE_PLAY_UNAVAILABLE)
                .hasCause(cause);
        }
    }

    @Nested
    @DisplayName("resolveQuantity 메서드는")
    class ResolveQuantityTests {

        @DisplayName("quantity가 null이면 1을 반환한다")
        @Test
        void whenQuantityNull_returnsOne() {
            ProductPurchase purchase = new ProductPurchase();

            assertThat(googlePlayClient.resolveQuantity(purchase)).isEqualTo(1);
        }

        @DisplayName("quantity가 주어지면 해당 값을 반환한다")
        @Test
        void whenQuantityPresent_returnsValue() {
            ProductPurchase purchase = new ProductPurchase().setQuantity(5);

            assertThat(googlePlayClient.resolveQuantity(purchase)).isEqualTo(5);
        }
    }
}
