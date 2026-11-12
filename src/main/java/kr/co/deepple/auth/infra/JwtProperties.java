package kr.co.deepple.auth.infra;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String issuer, String privateKey, Duration accessTokenLifetime) {
  @Override
  public String toString() {
    return "JwtProperties[issuer="
        + issuer
        + ", privateKey=****, accessTokenLifetime="
        + accessTokenLifetime
        + "]";
  }
}
