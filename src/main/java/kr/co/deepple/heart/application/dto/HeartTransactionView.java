package kr.co.deepple.heart.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import kr.co.deepple.heart.domain.TransactionType;

public record HeartTransactionView(
    long id,
    LocalDateTime createdAt,
    @Schema(implementation = TransactionType.class, description = "거래 종류. 앱 아이콘 구분에 쓴다")
        String transactionType,
    String content,
    long heartAmount) {
  @QueryProjection
  public HeartTransactionView {}
}
