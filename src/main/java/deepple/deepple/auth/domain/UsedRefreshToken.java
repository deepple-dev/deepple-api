package deepple.deepple.auth.domain;

import java.time.Instant;

public record UsedRefreshToken(TokenOwner owner, Instant usedAt) {
}
