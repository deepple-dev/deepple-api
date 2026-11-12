package kr.co.deepple.heart.application.port.in;

import lombok.NonNull;

public record UseHeartCommand(
    long memberId,
    @NonNull String usageType,
    @NonNull String usageSubtype,
    String content,
    @NonNull String idempotencyKey
) {
}
