package kr.co.deepple.heart.application.port.in;

import lombok.NonNull;

import java.time.Instant;

public record SyncHeartMemberCommand(
    long memberId,
    String gender,
    boolean isVip,
    @NonNull Instant sourceUpdatedAt
) {
}
