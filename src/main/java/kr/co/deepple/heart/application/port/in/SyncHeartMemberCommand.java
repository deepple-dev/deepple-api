package kr.co.deepple.heart.application.port.in;

import java.time.Instant;
import lombok.NonNull;

public record SyncHeartMemberCommand(
    long memberId, String gender, boolean isVip, @NonNull Instant sourceUpdatedAt) {}
