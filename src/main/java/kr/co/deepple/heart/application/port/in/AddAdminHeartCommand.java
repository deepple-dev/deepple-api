package kr.co.deepple.heart.application.port.in;

import lombok.NonNull;

public record AddAdminHeartCommand(
    long adminId, long memberId, long amount, @NonNull String reason, String idempotencyKey) {}
