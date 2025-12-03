package deepple.deepple.auth.application;

import deepple.deepple.auth.domain.TokenParser;
import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.auth.domain.TokenRepository;
import deepple.deepple.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static deepple.deepple.auth.application.AuthErrorStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final TokenParser tokenParser;
    private final TokenRepository tokenRepository;

    public AuthResponse authenticate(String accessToken, String refreshToken) {
        if (accessToken == null) {
            return AuthResponse.error(MISSING_ACCESS_TOKEN);
        }

        if (isValid(accessToken)) {
            return AuthResponse.authenticated(getId(accessToken), getRole(accessToken));
        }

        if (isExpired(accessToken)) {
            if (refreshToken == null) {
                log.info("[토큰 재발급 실패] 리프레시 토큰 누락");
                return AuthResponse.error(MISSING_REFRESH_TOKEN);
            }

            if (isExpired(refreshToken)) {
                log.info("[토큰 재발급 실패] 리프레시 토큰 만료");
                invalidateRefreshToken(refreshToken);
                return AuthResponse.error(EXPIRED_REFRESH_TOKEN);
            }

            if (!isValid(refreshToken) || !exists(refreshToken)) {
                log.warn("[토큰 재발급 실패] 리프레시 토큰 유효하지 않음");
                return AuthResponse.error(INVALID_REFRESH_TOKEN);
            }

            return reissueTokens(refreshToken);
        }

        log.warn("[토큰 인증 실패] 액세스 토큰 유효하지 않음");
        return AuthResponse.error(INVALID_ACCESS_TOKEN);
    }

    private AuthResponse reissueTokens(String refreshToken) {
        long id = getId(refreshToken);
        Role role = getRole(refreshToken);

        invalidateRefreshToken(refreshToken);

        Instant issuedAt = Instant.now();
        String reissuedAccessToken = createAccessToken(id, role, issuedAt);
        String reissuedRefreshToken = createRefreshToken(id, role, issuedAt);

        log.info("[토큰 재발급 완료] memberId={}", id);

        return AuthResponse.reissued(id, role, reissuedAccessToken, reissuedRefreshToken);
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

    private boolean exists(String token) {
        return tokenRepository.exists(token);
    }

    private void invalidateRefreshToken(String token) {
        boolean deleted = tokenRepository.delete(token);
        if (deleted) {
            log.info("[토큰 무효화 완료]");
        } else {
            log.warn("[토큰 무효화 실패] 토큰이 존재하지 않음");
        }
    }

    private String createAccessToken(long id, Role role, Instant issuedAt) {
        return tokenProvider.createAccessToken(id, role, issuedAt);
    }

    private String createRefreshToken(long id, Role role, Instant issuedAt) {
        String refreshToken = tokenProvider.createRefreshToken(id, role, issuedAt);
        tokenRepository.save(refreshToken);
        return refreshToken;
    }
}
