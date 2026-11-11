package kr.co.deepple.auth.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.util.List;

@Configuration
public class JwtConfig {

    @Bean
    public JwtEncoder jwtEncoder(JwtKeyPair keyPair) {
        return NimbusJwtEncoder.withKeyPair(keyPair.getPublicKey(), keyPair.getPrivateKey())
            .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtKeyPair keyPair, JwtProperties jwtProperties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(keyPair.getPublicKey())
            .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            new JwtExpirationValidator(),
            JwtValidators.createAtJwtValidator()
                .issuer(jwtProperties.issuer())
                .audience(JwtSpec.AUDIENCE)
                .validators(validators -> validators.put(JwtSpec.ROLES_CLAIM, rolesValidator()))
                .build()
        ));
        return decoder;
    }

    private static OAuth2TokenValidator<Jwt> rolesValidator() {
        return new JwtClaimValidator<List<String>>(JwtSpec.ROLES_CLAIM, roles -> roles != null && !roles.isEmpty());
    }
}
