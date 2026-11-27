package kr.co.deepple.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.auth.domain.WithdrawalReason;

public record MemberWithdrawRequest(
    @Schema(
            implementation = WithdrawalReason.class,
            description = "탈퇴 사유. 보내지 않으면 사유 없이 탈퇴한다. 본문을 보낼 때는 Content-Type: application/json",
            nullable = true)
        String reason) {}
