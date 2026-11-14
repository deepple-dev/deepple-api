package kr.co.deepple.payment.application.dto;

import jakarta.validation.constraints.NotBlank;

public record GooglePlayVerifyReceiptRequest(
    @NotBlank(message = "productId는 필수입니다.") String productId,
    @NotBlank(message = "purchaseToken은 필수입니다.") String purchaseToken) {}
