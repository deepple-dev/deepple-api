package kr.co.deepple.payment.command.infra.order;

import com.google.api.services.androidpublisher.model.ProductPurchase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FakeGooglePlayClient 단위 테스트")
class FakeGooglePlayClientTest {

    private final GooglePlayClient googlePlayClient = new FakeGooglePlayClient();

    @Test
    @DisplayName("모든 구매를 유효한 미소비 구매 1개로 돌려준다")
    void verifyPurchaseReturnsPurchased() {
        ProductPurchase purchase = googlePlayClient.verifyPurchase("heart_pack_100", "token");

        assertThat(purchase.getPurchaseState()).isZero();
        assertThat(purchase.getConsumptionState()).isNull();
        assertThat(purchase.getOrderId()).startsWith("LOCAL-");
        assertThat(googlePlayClient.resolveQuantity(purchase)).isEqualTo(1);
    }
}
