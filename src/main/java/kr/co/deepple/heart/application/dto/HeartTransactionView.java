package kr.co.deepple.heart.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record HeartTransactionView(
    Long id, LocalDateTime createdAt, String content, Long heartAmount) {
  @QueryProjection
  public HeartTransactionView {}
}
