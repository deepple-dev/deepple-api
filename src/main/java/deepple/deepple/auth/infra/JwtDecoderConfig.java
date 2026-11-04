package deepple.deepple.auth.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtDecoderConfig {

    private static final int HS512_MIN_KEY_BITS = 512;
    private static final int HS384_MIN_KEY_BITS = 384;

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes();
        MacAlgorithm algorithm = selectAlgorithm(keyBytes.length * Byte.SIZE);
        SecretKey key = new SecretKeySpec(keyBytes, algorithm.getName());

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(algorithm)
            .build();
        decoder.setJwtValidator(new JwtExpirationValidator());
        return decoder;
    }

    private MacAlgorithm selectAlgorithm(int keyBits) {
        if (keyBits >= HS512_MIN_KEY_BITS) {
            return MacAlgorithm.HS512;
        }
        if (keyBits >= HS384_MIN_KEY_BITS) {
            return MacAlgorithm.HS384;
        }
        return MacAlgorithm.HS256;
    }
}
