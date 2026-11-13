package kr.co.deepple.member.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.Gender;

public record AdminMemberView(
    long memberId,
    String nickname,
    @Schema(implementation = Gender.class) String gender,
    @Schema(implementation = ActivityStatus.class) String activityStatus,
    LocalDateTime joinedAt,
    int warningCount) {
  @QueryProjection
  public AdminMemberView {}
}
