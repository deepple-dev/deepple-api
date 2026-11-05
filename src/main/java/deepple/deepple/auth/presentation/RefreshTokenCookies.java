package deepple.deepple.auth.presentation;

import deepple.deepple.auth.infra.RefreshTokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookies {

    private final RefreshTokenProperties properties;

    public ResponseCookie create(String refreshToken) {
        return build(refreshToken, properties.lifetime());
    }

    public ResponseCookie expire() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration maxAge) {
        RefreshTokenProperties.Cookie cookie = properties.cookie();
        return ResponseCookie.from(cookie.name(), value)
            .httpOnly(cookie.httpOnly())
            .secure(cookie.secure())
            .sameSite(cookie.sameSite())
            .path(cookie.path())
            .maxAge(maxAge)
            .build();
    }
}
