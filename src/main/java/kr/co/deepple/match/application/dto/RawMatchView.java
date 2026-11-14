package kr.co.deepple.match.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record RawMatchView(
    long matchId,
    long opponentId,
    String opponentMessage,
    String myMessage,
    String matchStatus,
    LocalDateTime createdAt,
    LocalDateTime readAt) {

  @QueryProjection
  public RawMatchView {}
}
