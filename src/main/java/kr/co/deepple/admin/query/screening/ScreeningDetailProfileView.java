package kr.co.deepple.admin.query.screening;

import com.querydsl.core.annotations.QueryProjection;
import kr.co.deepple.admin.command.domain.screening.RejectionReasonType;
import kr.co.deepple.admin.command.domain.screening.ScreeningStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

public record ScreeningDetailProfileView(
    long memberId,
    @Schema(implementation = ScreeningStatus.class)
    String screeningStatus,
    @Schema(implementation = RejectionReasonType.class)
    String rejectionReason,
    String nickname,
    int age,
    @Schema(implementation = Gender.class)
    String gender,
    String joinedDate
) {
    @QueryProjection
    public ScreeningDetailProfileView {
    }
}
