package kr.co.deepple.member.query.member.view;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.PrimaryContactType;

// TODO : 푸시알림 설정 여부, 인터뷰 작성 여부, 지인차단 여부.
public record StatusInfo(
    Long memberId,
    @Schema(implementation = ActivityStatus.class) String activityStatus,
    Boolean isVip,
    @Schema(implementation = PrimaryContactType.class) String primaryContactType,
    boolean isDatingExamSubmitted) {}
