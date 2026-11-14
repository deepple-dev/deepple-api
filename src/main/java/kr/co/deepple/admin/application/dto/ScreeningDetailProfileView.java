package kr.co.deepple.admin.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.admin.domain.RejectionReasonType;
import kr.co.deepple.admin.domain.ScreeningStatus;
import kr.co.deepple.member.domain.Gender;

public record ScreeningDetailProfileView(
    long memberId,
    @Schema(implementation = ScreeningStatus.class) String screeningStatus,
    @Schema(implementation = RejectionReasonType.class) String rejectionReason,
    String nickname,
    int age,
    @Schema(implementation = Gender.class) String gender,
    String joinedDate) {
  @QueryProjection
  public ScreeningDetailProfileView {}
}
