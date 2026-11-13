package kr.co.deepple.member.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.member.domain.PrimaryContactType;

public record AdminMemberStatusInfo(
    @Schema(implementation = PrimaryContactType.class) String primaryContactType,
    boolean hasInterviewAnswer,
    int warningCount,
    boolean isDatingExamSubmitted) {
  @QueryProjection
  public AdminMemberStatusInfo {}
}
