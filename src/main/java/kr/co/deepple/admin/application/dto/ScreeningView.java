package kr.co.deepple.admin.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.admin.domain.RejectionReasonType;
import kr.co.deepple.admin.domain.ScreeningStatus;
import kr.co.deepple.member.domain.Gender;

public record ScreeningView(
    long screeningId,
    String nickname,
    @Schema(implementation = Gender.class) String gender,
    String joinedDate,
    @Schema(implementation = ScreeningStatus.class) String screeningStatus,
    @Schema(implementation = RejectionReasonType.class) String rejectionReason) {
  @QueryProjection
  public ScreeningView {}
}
