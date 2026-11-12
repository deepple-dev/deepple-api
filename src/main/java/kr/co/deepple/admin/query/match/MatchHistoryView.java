package kr.co.deepple.admin.query.match;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.match.command.domain.match.MatchStatus;

public record MatchHistoryView(
    long matchId,
    String requesterNickname,
    String responderNickname,
    String requestMessage,
    String responseMessage,
    @Schema(implementation = MatchStatus.class) String status,
    String readByResponderAt) {
  @QueryProjection
  public MatchHistoryView {}
}
