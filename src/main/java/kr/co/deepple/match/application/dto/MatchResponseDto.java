package kr.co.deepple.match.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.deepple.match.domain.MatchContactType;

public record MatchResponseDto(
    @NotBlank String responseMessage,
    @Schema(implementation = MatchContactType.class) String contactType) {}
