package kr.co.deepple.member.query.member.view;

import com.querydsl.core.annotations.QueryProjection;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminMemberView(
    long memberId,
    String nickname,
    @Schema(implementation = Gender.class)
    String gender,
    @Schema(implementation = ActivityStatus.class)
    String activityStatus,
    LocalDateTime joinedAt,
    int warningCount
) {
    @QueryProjection
    public AdminMemberView {
    }
}
