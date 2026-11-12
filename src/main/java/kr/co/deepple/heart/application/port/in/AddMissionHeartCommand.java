package kr.co.deepple.heart.application.port.in;

import lombok.NonNull;

public record AddMissionHeartCommand(
    long memberId,
    long amount,
    @NonNull String actionType
) {
}
