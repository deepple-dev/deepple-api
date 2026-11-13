package kr.co.deepple.member.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.PrimaryContactType;

public record StatusInfo(
    Long memberId,
    @Schema(implementation = ActivityStatus.class) String activityStatus,
    Boolean isVip,
    @Schema(implementation = PrimaryContactType.class) String primaryContactType,
    boolean isDatingExamSubmitted) {}
