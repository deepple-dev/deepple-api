package kr.co.deepple.payment.command.infra.order;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.time.Duration;
import java.util.List;
import kr.co.deepple.common.infra.google.GoogleWorkloadIdentityCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

@Slf4j
@Configuration
@ConditionalOnProperty(
    name = "payment.google-play.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class GooglePlayClientConfig {

  @Bean
  public AndroidPublisher androidPublisher(
      AwsCredentialsProvider awsCredentialsProvider,
      AwsRegionProvider awsRegionProvider,
      @Value("${google.workload-identity.audience}") String audience,
      @Value("${payment.google-play.service-account}") String serviceAccount,
      @Value("${payment.google-play.application-name}") String applicationName,
      @Value("${payment.google-play.connect-timeout}") Duration connectTimeout,
      @Value("${payment.google-play.read-timeout}") Duration readTimeout) {
    GoogleCredentials credentials =
        GoogleWorkloadIdentityCredentials.create(
                audience, serviceAccount, awsCredentialsProvider, awsRegionProvider.getRegion())
            .createScoped(List.of(AndroidPublisherScopes.ANDROIDPUBLISHER));

    return androidPublisherBuilder(
            new HttpCredentialsAdapter(credentials), applicationName, connectTimeout, readTimeout)
        .build();
  }

  static AndroidPublisher.Builder androidPublisherBuilder(
      HttpRequestInitializer credentials,
      String applicationName,
      Duration connectTimeout,
      Duration readTimeout) {
    return new AndroidPublisher.Builder(
            new NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            request -> {
              credentials.initialize(request);
              request.setConnectTimeout(Math.toIntExact(connectTimeout.toMillis()));
              request.setReadTimeout(Math.toIntExact(readTimeout.toMillis()));
            })
        .setApplicationName(applicationName);
  }
}
