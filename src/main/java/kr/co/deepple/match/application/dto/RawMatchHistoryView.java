package kr.co.deepple.match.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record RawMatchHistoryView(
    long matchId,
    long requesterId,
    long responderId,
    String requestMessage,
    String responseMessage,
    String status,
    String readByResponderAt) {

  @QueryProjection
  public RawMatchHistoryView {}
}
