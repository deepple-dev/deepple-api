package kr.co.deepple.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DeviceRegisterRequest(
    @Schema(description = "디바이스 고유 ID") @NotBlank(message = "디바이스 ID를 입력해주세요.") String deviceId,
    @Schema(description = "디바이스 토큰") @NotBlank(message = "디바이스 토큰을 입력해주세요.")
        String registrationToken) {}
