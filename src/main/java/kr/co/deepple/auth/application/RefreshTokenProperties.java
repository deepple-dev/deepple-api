package kr.co.deepple.auth.application;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.refresh-token")
public record RefreshTokenProperties(Duration lifetime, Cookie cookie) {
  public record Cookie(
      String name, String path, String sameSite, boolean secure, boolean httpOnly) {}
}
