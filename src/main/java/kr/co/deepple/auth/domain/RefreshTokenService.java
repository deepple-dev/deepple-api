package kr.co.deepple.auth.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import kr.co.deepple.auth.infra.RefreshTokenProperties;
import kr.co.deepple.auth.infra.RefreshTokenRepository;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RefreshTokenService {

  private static final int TOKEN_BYTES = 32;
  private static final Duration REUSE_GRACE_PERIOD = Duration.ofSeconds(30);

  private final RefreshTokenRepository refreshTokenRepository;
  private final Duration lifetime;
  private final StringKeyGenerator tokenGenerator =
      new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), TOKEN_BYTES);

  public RefreshTokenService(
      RefreshTokenRepository refreshTokenRepository, RefreshTokenProperties properties) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.lifetime = properties.lifetime();
  }

  public String issue(TokenOwner owner) {
    String token = tokenGenerator.generateKey();
    refreshTokenRepository.save(hash(token), owner, lifetime);
    return token;
  }

  public TokenOwner consume(String token) {
    if (token == null || token.isBlank()) {
      throw new DeeppleException(AuthErrorCode.MISSING_REFRESH_TOKEN);
    }

    String tokenHash = hash(token);
    Instant now = Instant.now();
    TokenOwner activeOwner = refreshTokenRepository.findOwner(tokenHash).orElse(null);
    if (activeOwner != null
        && refreshTokenRepository.markUsed(tokenHash, activeOwner, now, lifetime)) {
      refreshTokenRepository.delete(tokenHash, activeOwner);
      return activeOwner;
    }

    UsedRefreshToken used =
        refreshTokenRepository
            .findUsed(tokenHash)
            .orElseThrow(() -> new DeeppleException(AuthErrorCode.INVALID_REFRESH_TOKEN));
    if (used.usedAt().plus(REUSE_GRACE_PERIOD).isAfter(now)) {
      return used.owner();
    }

    log.atWarn()
        .addKeyValue("ownerRole", used.owner().role())
        .addKeyValue("ownerId", used.owner().id())
        .log("리프레시 토큰 재사용 감지, 모든 리프레시 토큰 폐기");
    refreshTokenRepository.deleteAll(used.owner());
    throw new DeeppleException(AuthErrorCode.INVALID_REFRESH_TOKEN);
  }

  public void revoke(String token) {
    if (token == null || token.isBlank()) {
      return;
    }

    String tokenHash = hash(token);
    refreshTokenRepository
        .findOwner(tokenHash)
        .ifPresent(owner -> refreshTokenRepository.delete(tokenHash, owner));
  }

  public void revokeAll(TokenOwner owner) {
    refreshTokenRepository.deleteAll(owner);
  }

  private String hash(String token) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
    }
  }
}
