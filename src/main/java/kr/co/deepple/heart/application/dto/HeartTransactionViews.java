package kr.co.deepple.heart.application.dto;

import java.util.List;

public record HeartTransactionViews(
    List<HeartTransactionView> transactions,
    boolean hasMore
) {
}
