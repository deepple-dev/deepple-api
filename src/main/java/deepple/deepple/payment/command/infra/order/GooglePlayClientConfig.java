package deepple.deepple.payment.command.infra.order;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import deepple.deepple.common.infra.google.GoogleWorkloadIdentityCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "payment.google-play.enabled", havingValue = "true", matchIfMissing = true)
public class GooglePlayClientConfig {

    @Bean
    public AndroidPublisher androidPublisher(
        AwsCredentialsProvider awsCredentialsProvider,
        AwsRegionProvider awsRegionProvider,
        @Value("${google.workload-identity.audience}") String audience,
        @Value("${payment.google-play.service-account}") String serviceAccount,
        @Value("${payment.google-play.application-name}") String applicationName
    ) {
        GoogleCredentials credentials = GoogleWorkloadIdentityCredentials.create(
                audience,
                serviceAccount,
                awsCredentialsProvider,
                awsRegionProvider.getRegion()
            )
            .createScoped(List.of(AndroidPublisherScopes.ANDROIDPUBLISHER));

        try {
            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

            return new AndroidPublisher.Builder(
                transport,
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
            )
                .setApplicationName(applicationName)
                .build();
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Google Play 클라이언트 초기화 실패: " + e.getMessage(), e);
        }
    }
}
