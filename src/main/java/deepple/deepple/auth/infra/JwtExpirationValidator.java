package deepple.deepple.auth.infra;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

public class JwtExpirationValidator implements OAuth2TokenValidator<Jwt> {

    public static final String EXPIRED_ERROR_CODE = "expired_token";

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(EXPIRED_ERROR_CODE, "만료된 토큰입니다.", null));
        }
        return OAuth2TokenValidatorResult.success();
    }
}
