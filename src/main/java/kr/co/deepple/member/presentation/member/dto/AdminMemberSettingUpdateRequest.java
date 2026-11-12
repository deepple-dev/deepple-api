package kr.co.deepple.member.presentation.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.member.command.domain.member.Grade;

public record AdminMemberSettingUpdateRequest(
    @Schema(implementation = Grade.class) String grade,
    boolean isVip,
    boolean isPushNotificationEnabled) {}
