package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminMemberSettingInfo(
    @Schema(description = "등급. 값은 회원의 grade와 같다", example = "DIAMOND") String grade,
    boolean isProfilePublic,
    @Schema(description = "활동 상태. 값은 회원의 activityStatus와 같다", example = "ACTIVE")
        String activityStatus,
    boolean isVip,
    boolean isPushNotificationEnabled) {}
