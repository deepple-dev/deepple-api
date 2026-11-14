package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record AdminMemberView(
    long memberId,
    String nickname,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다", example = "MALE") String gender,
    @Schema(description = "활동 상태. 값은 회원의 activityStatus와 같다", example = "ACTIVE")
        String activityStatus,
    LocalDateTime joinedAt,
    int warningCount) {}
