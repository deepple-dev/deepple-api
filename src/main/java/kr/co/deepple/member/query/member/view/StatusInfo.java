package kr.co.deepple.member.query.member.view;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.PrimaryContactType;

public record StatusInfo(
    Long memberId,
    @Schema(implementation = ActivityStatus.class) String activityStatus,
    Boolean isVip,
    @Schema(implementation = PrimaryContactType.class) String primaryContactType,
    boolean isDatingExamSubmitted) {}
