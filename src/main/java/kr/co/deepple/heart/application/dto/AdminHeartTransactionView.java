package kr.co.deepple.heart.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import kr.co.deepple.heart.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminHeartTransactionView(
    Long id,
    LocalDateTime createdAt,
    @Schema(implementation = TransactionType.class)
    String transactionType,
    String content,
    Long heartAmount,
    Long heartBalance
) {
    @QueryProjection
    public AdminHeartTransactionView {
    }
}
