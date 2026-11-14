package kr.co.deepple.admin.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.match.domain.MatchStatus;

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
