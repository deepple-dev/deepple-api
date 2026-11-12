package kr.co.deepple.admin.query.screening;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.admin.command.domain.screening.RejectionReasonType;
import kr.co.deepple.admin.command.domain.screening.ScreeningStatus;
import kr.co.deepple.member.command.domain.member.Gender;

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
