package kr.co.deepple.member.query.member.view;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Grade;

public record AdminMemberSettingInfo(
    @Schema(implementation = Grade.class) String grade,
    boolean isProfilePublic,
    @Schema(implementation = ActivityStatus.class) String activityStatus,
    boolean isVip,
    boolean isPushNotificationEnabled) {
  @QueryProjection
  public AdminMemberSettingInfo {}
}
