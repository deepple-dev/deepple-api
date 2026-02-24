package deepple.deepple.auth.application;

import deepple.deepple.auth.domain.TokenParser;
import deepple.deepple.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static deepple.deepple.auth.application.AuthErrorStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenParser tokenParser;

    public AuthResponse authenticate(String accessToken) {
        if (accessToken == null) {
            return AuthResponse.error(MISSING_ACCESS_TOKEN);
        }

        if (isValid(accessToken)) {
            return AuthResponse.authenticated(getId(accessToken), getRole(accessToken));
        }

        if (isExpired(accessToken)) {
            return AuthResponse.error(EXPIRED_ACCESS_TOKEN);
        }

        log.warn("[토큰 인증 실패] 액세스 토큰 유효하지 않음");
        return AuthResponse.error(INVALID_ACCESS_TOKEN);
    }

    private Role getRole(String token) {
        return tokenParser.getRole(token);
    }

    private long getId(String token) {
        return tokenParser.getId(token);
    }

    private boolean isValid(String token) {
        return tokenParser.isValid(token);
    }

    private boolean isExpired(String token) {
        return tokenParser.isExpired(token);
    }
}
