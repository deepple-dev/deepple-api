package kr.co.deepple.auth.infra;

import kr.co.deepple.auth.domain.TokenProvider;
import kr.co.deepple.common.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider implements TokenProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public String createAccessToken(long id, Role role, Instant issuedAt) {
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
            .type(JwtSpec.ACCESS_TOKEN_TYPE)
            .build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .subject(String.valueOf(id))
            .audience(List.of(JwtSpec.AUDIENCE))
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plus(jwtProperties.accessTokenLifetime()))
            .id(UUID.randomUUID().toString())
            .claim(JwtSpec.ROLES_CLAIM, List.of(role.name()))
            .claim(JwtSpec.CLIENT_ID_CLAIM, JwtSpec.clientIdOf(role))
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
            .getTokenValue();
    }
}
