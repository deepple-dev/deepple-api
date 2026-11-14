package kr.co.deepple.payment.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record HeartPurchaseOptionCreateRequest(
    @Min(value = 1, message = "최소 1 이상의 하트 수량을 입력해주세요.") Long heartAmount,
    @Min(value = 1, message = "최소 1 이상의 가격을 입력해주세요.") Long price,
    @NotBlank(message = "상품 ID를 입력해주세요.") String productId,
    @NotBlank(message = "상품 이름을 입력해주세요.") String name) {}
