package kr.co.deepple.auth.application.port.out;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.domain.UsedRefreshToken;

public interface RefreshTokenRepository {
  void save(String tokenHash, TokenOwner owner, Duration lifetime);

  Optional<TokenOwner> findOwner(String tokenHash);

  boolean markUsed(String tokenHash, TokenOwner owner, Instant usedAt, Duration lifetime);

  Optional<UsedRefreshToken> findUsed(String tokenHash);

  void delete(String tokenHash, TokenOwner owner);

  void deleteAll(TokenOwner owner);
}
