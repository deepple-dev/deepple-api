package kr.co.deepple.heart.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record HeartTransactionView(
    long id, LocalDateTime createdAt, String content, long heartAmount) {
  @QueryProjection
  public HeartTransactionView {}
}
