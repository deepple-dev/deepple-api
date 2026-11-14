package kr.co.deepple.payment.application.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyReceiptRequest(@NotBlank(message = "앱 영수증은 필수입니다.") String appReceipt) {}
