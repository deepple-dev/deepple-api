package kr.co.deepple.auth.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "auth.refresh-token")
public record RefreshTokenProperties(
    Duration lifetime,
    Cookie cookie
) {

    public record Cookie(
        String name,
        String path,
        String sameSite,
        boolean secure,
        boolean httpOnly
    ) {
    }
}
