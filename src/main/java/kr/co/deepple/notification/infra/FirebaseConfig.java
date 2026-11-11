package kr.co.deepple.notification.infra;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import kr.co.deepple.common.infra.google.GoogleWorkloadIdentityCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
public class FirebaseConfig {

    private final AwsCredentialsProvider awsCredentialsProvider;
    private final AwsRegionProvider awsRegionProvider;

    @Value("${google.workload-identity.audience}")
    private String audience;

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.service-account}")
    private String serviceAccount;

    @Value("${firebase.connect-timeout}")
    private Duration connectTimeout;

    @Value("${firebase.read-timeout}")
    private Duration readTimeout;

    @Value("${firebase.write-timeout}")
    private Duration writeTimeout;

    @PostConstruct
    private void init() {
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleWorkloadIdentityCredentials.create(
                audience,
                serviceAccount,
                awsCredentialsProvider,
                awsRegionProvider.getRegion()
            ))
            .setProjectId(projectId)
            .setConnectTimeout(Math.toIntExact(connectTimeout.toMillis()))
            .setReadTimeout(Math.toIntExact(readTimeout.toMillis()))
            .setWriteTimeout(Math.toIntExact(writeTimeout.toMillis()))
            .build();

        FirebaseApp.initializeApp(options);
    }
}
