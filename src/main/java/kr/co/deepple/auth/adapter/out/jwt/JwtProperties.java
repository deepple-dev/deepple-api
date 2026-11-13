package kr.co.deepple.auth.adapter.out.jwt;

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
