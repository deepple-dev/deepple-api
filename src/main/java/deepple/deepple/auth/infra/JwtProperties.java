package deepple.deepple.auth.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String issuer,
    String privateKey,
    Duration accessTokenLifetime
) {

    @Override
    public String toString() {
        return "JwtProperties[issuer=" + issuer + ", privateKey=****, accessTokenLifetime=" + accessTokenLifetime + "]";
    }
}
