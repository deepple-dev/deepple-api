package kr.co.deepple.member.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.member.domain.ActivityStatus;

public record MemberStatusResponse(
    @Schema(implementation = ActivityStatus.class) String activityStatus,
    @Schema(description = "프로필을 아직 작성하지 않아 프로필 작성 화면으로 보내야 하는지 여부")
        boolean isProfileSettingNeeded) {}
