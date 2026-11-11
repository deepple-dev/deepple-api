package kr.co.deepple.auth.domain;

import java.time.Instant;

public record UsedRefreshToken(TokenOwner owner, Instant usedAt) {
}
