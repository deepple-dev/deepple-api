package deepple.deepple.auth.infra;

import deepple.deepple.auth.domain.TokenParser;
import deepple.deepple.common.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTypeValidator;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class JwtParser implements TokenParser {

    private final JwtDecoder refreshTokenDecoder;

    public JwtParser(JwtKeyPair keyPair, @Value("${jwt.issuer}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(keyPair.getPublicKey())
            .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            new JwtTypeValidator(JwtSpec.REFRESH_TOKEN_TYPE),
            new JwtIssuerValidator(issuer),
            new JwtExpirationValidator()
        ));
        this.refreshTokenDecoder = decoder;
    }

    public boolean isValid(String token) {
        try {
            refreshTokenDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isExpired(String token) {
        try {
            refreshTokenDecoder.decode(token);
            return false;
        } catch (JwtValidationException e) {
            return e.getErrors()
                .stream()
                .anyMatch(error -> JwtExpirationValidator.EXPIRED_ERROR_CODE.equals(error.getErrorCode()));
        } catch (JwtException e) {
            return false;
        }
    }

    public long getId(String token) {
        return Long.parseLong(refreshTokenDecoder.decode(token)
            .getSubject());
    }

    public Role getRole(String token) {
        Jwt jwt = refreshTokenDecoder.decode(token);
        return Role.valueOf(jwt.getClaimAsStringList(JwtSpec.ROLES_CLAIM)
            .getFirst());
    }
}
