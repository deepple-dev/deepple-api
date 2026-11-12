package kr.co.deepple.auth.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

    @Bean
    public JwtEncoder jwtEncoder(JwtKeyPair keyPair) {
        return NimbusJwtEncoder.withKeyPair(keyPair.getPublicKey(), keyPair.getPrivateKey())
            .build();
    }
}
