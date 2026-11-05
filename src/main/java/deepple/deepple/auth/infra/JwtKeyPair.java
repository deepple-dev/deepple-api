package deepple.deepple.auth.infra;

import lombok.Getter;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

@Getter
@Component
public class JwtKeyPair {

    private static final String RSA = "RSA";

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtKeyPair(JwtProperties jwtProperties) {
        this.privateKey = RsaKeyConverters.pkcs8()
            .convert(new ByteArrayInputStream(jwtProperties.privateKey().getBytes(StandardCharsets.UTF_8)));
        this.publicKey = derivePublicKey(privateKey);
    }

    private static RSAPublicKey derivePublicKey(RSAPrivateKey privateKey) {
        if (!(privateKey instanceof RSAPrivateCrtKey crtKey)) {
            throw new IllegalStateException("공개 키를 계산할 수 없는 RSA 개인 키 형식입니다.");
        }

        try {
            RSAPublicKeySpec spec = new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent());
            return (RSAPublicKey) KeyFactory.getInstance(RSA)
                .generatePublic(spec);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("RSA 공개 키를 만들 수 없습니다.", e);
        }
    }
}
