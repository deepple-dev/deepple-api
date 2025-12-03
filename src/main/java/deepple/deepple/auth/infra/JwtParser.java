package deepple.deepple.auth.infra;

import deepple.deepple.auth.domain.TokenParser;
import deepple.deepple.common.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
public class JwtParser implements TokenParser {

    private static final String ROLE = "role";
    private final Key key;

    public JwtParser(@Value("${jwt.secret}") String secret) {
        key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public boolean isValid(String token) {
        try {
            parseJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isExpired(String token) {
        try {
            parseJws(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public long getId(String token) {
        return Long.parseLong(getSubject(token));
    }

    public Role getRole(String token) {
        return Role.valueOf(getClaim(token, ROLE, String.class));
    }

    public Instant getExpiration(String token) {
        return getExpirationDate(token)
            .toInstant()
            .truncatedTo(ChronoUnit.SECONDS);
    }

    private Jws<Claims> parseJws(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token);
    }

    private Claims parseClaims(String token) {
        return parseJws(token).getBody();
    }

    private String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    private Date getExpirationDate(String token) {
        return parseClaims(token).getExpiration();
    }

    private <T> T getClaim(String token, String claimName, Class<T> requiredType) {
        return parseClaims(token).get(claimName, requiredType);
    }
}
