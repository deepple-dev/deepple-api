package kr.co.deepple.member.query.member.view;

import com.querydsl.core.annotations.QueryProjection;
import kr.co.deepple.member.command.domain.member.PrimaryContactType;
import io.swagger.v3.oas.annotations.media.Schema;

// TODO : 지인차단 여부 추가
public record AdminMemberStatusInfo(
    @Schema(implementation = PrimaryContactType.class)
    String primaryContactType,
    boolean hasInterviewAnswer,
    int warningCount,
    boolean isDatingExamSubmitted
) {
    @QueryProjection
    public AdminMemberStatusInfo {
    }
}
