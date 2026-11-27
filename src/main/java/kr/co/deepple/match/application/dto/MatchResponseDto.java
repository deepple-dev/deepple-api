package kr.co.deepple.match.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.deepple.match.domain.MatchContactType;

public record MatchResponseDto(
    @NotBlank String responseMessage,
    @NotBlank(message = "연락처 타입을 설정해주세요") @Schema(implementation = MatchContactType.class)
        String contactType) {}
