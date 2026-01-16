package deepple.deepple.member.presentation.member.dto;

import deepple.deepple.member.command.domain.member.Grade;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminMemberSettingUpdateRequest(
    @Schema(implementation = Grade.class)
    String grade,
    boolean isVip,
    boolean isPushNotificationEnabled
) {
}
