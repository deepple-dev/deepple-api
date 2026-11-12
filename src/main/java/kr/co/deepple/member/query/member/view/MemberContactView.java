package kr.co.deepple.member.query.member.view;

import com.querydsl.core.annotations.QueryProjection;
import kr.co.deepple.member.command.domain.member.PrimaryContactType;
import io.swagger.v3.oas.annotations.media.Schema;

public record MemberContactView(
    String phoneNumber,
    String kakaoId,
    @Schema(implementation = PrimaryContactType.class)
    String primaryContactType
) {
    @QueryProjection
    public MemberContactView {
    }
}
