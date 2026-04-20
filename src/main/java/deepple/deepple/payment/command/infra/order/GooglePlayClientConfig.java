package deepple.deepple.payment.command.infra.order;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Configuration
public class GooglePlayClientConfig {

    @Bean
    public AndroidPublisher androidPublisher(
        @Value("${payment.google-play.service-account-key-path}") String serviceAccountKeyPath,
        @Value("${payment.google-play.application-name}") String applicationName
    ) {
        try (FileInputStream keyStream = new FileInputStream(serviceAccountKeyPath)) {
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(keyStream)
                .createScoped(List.of(AndroidPublisherScopes.ANDROIDPUBLISHER));
            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

            return new AndroidPublisher.Builder(
                transport,
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
            )
                .setApplicationName(applicationName)
                .build();
        } catch (IOException e) {
            throw new UncheckedIOException("Google Play Service Account 키 파일을 읽을 수 없습니다: " + e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Google Play 클라이언트 초기화 실패: " + e.getMessage(), e);
        }
    }
}
