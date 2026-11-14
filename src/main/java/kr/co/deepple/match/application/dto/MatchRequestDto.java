package kr.co.deepple.match.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.deepple.match.domain.MatchContactType;

public record MatchRequestDto(
    @NotNull(message = "응답자 아이디를 입력해주세요.") Long responderId,
    @NotBlank(message = "메세지를 입력해주세요.") String requestMessage,
    @NotBlank(message = "연락처 타입을 설정해주세요") @Schema(implementation = MatchContactType.class)
        String contactType) {}
