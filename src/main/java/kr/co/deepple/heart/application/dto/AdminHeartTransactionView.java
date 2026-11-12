package kr.co.deepple.heart.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.heart.domain.TransactionType;

public record AdminHeartTransactionView(
    long id,
    LocalDateTime createdAt,
    @Schema(implementation = TransactionType.class) String transactionType,
    String content,
    long heartAmount,
    long heartBalance) {
  @QueryProjection
  public AdminHeartTransactionView {}
}
