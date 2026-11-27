package kr.co.deepple.member.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.member.domain.ActivityStatus;

public record MemberStatusResponse(
    @Schema(implementation = ActivityStatus.class) String activityStatus,
    @Schema(description = "프로필을 아직 작성하지 않아 프로필 작성 화면으로 보내야 하는지 여부") boolean isProfileSettingNeeded,
    @Schema(description = "닉네임. 프로필을 작성하지 않았으면 null이다") String nickname,
    @Schema(description = "대표 프로필 사진. 없으면 null이다") String profileImageUrl,
    @Schema(
            description =
                "심사 반려 사유 코드. 활동 상태가 REJECTED_SCREENING일 때만 내려 준다 - STOLEN_IMAGE: 사진 도용, "
                    + "INAPPROPRIATE_IMAGE: 부적절한 사진, EXPLICIT_CONTENT: 과도한 성적 표현, "
                    + "OFFENSIVE_LANGUAGE: 욕설 및 불쾌감을 주는 표현, CONTACT_IN_PROFILE: 프로필 내 연락처 기재",
            example = "INAPPROPRIATE_IMAGE")
        String rejectionReason,
    @Schema(description = "심사 반려 사유 이름. 활동 상태가 REJECTED_SCREENING일 때만 내려 준다", example = "부적절한 사진")
        String rejectionReasonDescription) {}
