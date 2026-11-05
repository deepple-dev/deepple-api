package deepple.deepple.notification.presentation;

import deepple.deepple.notification.command.domain.NotificationType;
import deepple.deepple.notification.command.domain.SenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record NotificationTestSendRequest(
    @Schema(description = "알림 타입", example = "LIKE")
    @NotNull(message = "알림 타입을 입력해주세요.")
    NotificationType type,

    @Schema(description = "받는 회원 ID. 생략하면 본인", example = "1")
    Long receiverId,

    @Schema(description = "보내는 사람 타입. 생략하면 MEMBER", example = "ADMIN")
    SenderType senderType,

    @Schema(description = "보내는 사람 ID. 생략하면 본인", example = "1")
    Long senderId,

    @Schema(description = "템플릿 파라미터. 생략하면 빈 값", example = "{\"senderName\": \"테스트\"}")
    Map<String, String> params
) {
}
