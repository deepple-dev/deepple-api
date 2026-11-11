package deepple.deepple.payment.command.infra.order;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
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

import java.util.List;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "payment.google-play.enabled", havingValue = "true", matchIfMissing = true)
public class GooglePlayClientConfig {

    private static final int CONNECT_TIMEOUT_MILLIS = 3_000;
    private static final int READ_TIMEOUT_MILLIS = 5_000;

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

        return androidPublisherBuilder(new HttpCredentialsAdapter(credentials), applicationName).build();
    }

    static AndroidPublisher.Builder androidPublisherBuilder(HttpRequestInitializer credentials, String applicationName) {
        return new AndroidPublisher.Builder(
            new NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            request -> {
                credentials.initialize(request);
                request.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
                request.setReadTimeout(READ_TIMEOUT_MILLIS);
            }
        )
            .setApplicationName(applicationName);
    }
}
