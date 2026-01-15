package deepple.deepple.payment.command.infra.order;

import com.apple.itunes.storekit.model.Environment;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class AppStoreClientConfig {

    @Bean
    public SignedDataVerifier signedDataVerifier(
        @Value("${payment.app-store.root-ca-paths}") String[] rootCaPaths,
        @Value("${payment.app-store.bundle-id}") String bundleId,
        @Value("${payment.app-store.app-apple-id}") Long appAppleId,
        @Value("${payment.app-store.environment}") String environmentValue) {
        Environment environment = Environment.fromValue(environmentValue);
        Set<InputStream> rootCAs = Arrays.stream(rootCaPaths)
            .map(path -> {
                try {
                    return new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    throw new UncheckedIOException("인증서 파일을 찾을 수 없습니다: " + path, e);
                }
            })
            .collect(Collectors.toSet());
        try {
            if (environment.equals(Environment.SANDBOX)) {
                appAppleId = null;
            }
            return new SignedDataVerifier(rootCAs, bundleId, appAppleId, environment, true);
        } finally {
            rootCAs.forEach(is -> {
                try {
                    is.close();
                } catch (IOException e) {
                    log.warn("Failed to close InputStream", e);
                }
            });
        }
    }
}
