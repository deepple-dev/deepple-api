package deepple.deepple.payment.presentation.order.dto;

import jakarta.validation.constraints.NotBlank;

public record GooglePlayVerifyReceiptRequest(
    @NotBlank(message = "productId는 필수입니다.")
    String productId,

    @NotBlank(message = "purchaseToken은 필수입니다.")
    String purchaseToken
) {
}
