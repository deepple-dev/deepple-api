package kr.co.deepple.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.List;

@Configuration
public class JwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(JwtPublicKeySource publicKeySource, JwtVerificationProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKeySource.getPublicKey())
            .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            new JwtExpirationValidator(),
            JwtValidators.createAtJwtValidator()
                .issuer(properties.issuer())
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
