package kr.co.deepple.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtVerificationProperties(
    String issuer
) {
}
