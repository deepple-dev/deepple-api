package kr.co.deepple.feed.application.port.in;

import java.time.Instant;
import lombok.NonNull;

public record SyncFeedAuthorProfileCommand(
    long memberId,
    String nickname,
    String gender,
    Integer yearOfBirth,
    String city,
    String mbti,
    @NonNull Instant occurredAt) {}
