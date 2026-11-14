package kr.co.deepple.match.application.dto;

import java.time.LocalDateTime;

public record MatchView(
    long matchId,
    long opponentId,
    String opponentMessage,
    String nickName,
    String profileImageUrl,
    String city,
    String myMessage,
    String matchStatus,
    LocalDateTime createdAt,
    LocalDateTime readAt // 상대가 내 요청을 읽은 시각, null이면 안읽음
    ) {}
