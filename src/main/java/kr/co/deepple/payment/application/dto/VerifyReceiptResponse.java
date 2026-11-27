package kr.co.deepple.payment.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record VerifyReceiptResponse(
    @Schema(description = "이 거래로 구매한 하트 개수. 같은 거래를 다시 확인해도 같은 값이다") long purchasedHeartAmount) {}
