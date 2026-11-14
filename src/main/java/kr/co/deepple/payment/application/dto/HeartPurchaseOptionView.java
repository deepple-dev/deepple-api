package kr.co.deepple.payment.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record HeartPurchaseOptionView(
    Long id,
    String name,
    String productId,
    Long heartAmount,
    Long price,
    LocalDateTime createdAt,
    LocalDateTime deletedAt) {
  @QueryProjection
  public HeartPurchaseOptionView {}
}
