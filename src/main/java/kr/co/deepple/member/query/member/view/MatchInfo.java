package kr.co.deepple.member.query.member.view;

import kr.co.deepple.match.command.domain.match.MatchContactType;
import kr.co.deepple.match.command.domain.match.MatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record MatchInfo(
    Long matchId,
    Long requesterId,
    Long responderId,
    String requestMessage,
    String responseMessage,
    @Schema(implementation = MatchStatus.class)
    String matchStatus,
    @Schema(implementation = MatchContactType.class)
    String requesterContactType,
    @Schema(implementation = MatchContactType.class)
    String responderContactType
) {
}
