package kr.co.deepple.notification.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record NotificationPreferenceSetRequest(
    @Schema(
        description = "알림 타입별 설정 (true: 허용, false: 거부)",
        example = """
            {
                "MATCH_REQUEST": true,
                "MATCH_ACCEPT": true,
                "LIKE": false,
                "PROFILE_EXCHANGE_REQUEST": true
            }
            """
    )
    @NotNull(message = "알림 설정을 입력해주세요.")
    Map<String, Boolean> preferences
) {
}
