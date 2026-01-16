package deepple.deepple.payment.presentation.order.dto;

import jakarta.validation.constraints.NotBlank;

public record AppleNotificationRequest(
    @NotBlank(message = "signedPayload는 필수입니다.")
    String signedPayload
) {
}