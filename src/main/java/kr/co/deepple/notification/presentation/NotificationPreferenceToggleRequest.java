package kr.co.deepple.notification.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.deepple.notification.command.domain.NotificationType;

public record NotificationPreferenceToggleRequest(
    @Schema(description = "알림 타입", example = "LIKE", implementation = NotificationType.class)
        @NotBlank(message = "알림 타입을 입력해주세요.")
        String type) {}
