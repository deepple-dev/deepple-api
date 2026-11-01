package deepple.deepple.common.config;

import io.awspring.cloud.s3.S3Template;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

@TestConfiguration
public class S3TestConfig {
    @Bean
    public S3Template s3Template() {
        return Mockito.mock(S3Template.class);
    }

    @Bean
    public S3Client s3Client() {
        return Mockito.mock(S3Client.class);
    }
}
