package kr.co.deepple.member.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.member.domain.Grade;

public record AdminMemberSettingUpdateRequest(
    @Schema(implementation = Grade.class) String grade,
    boolean isVip,
    boolean isPushNotificationEnabled) {}
