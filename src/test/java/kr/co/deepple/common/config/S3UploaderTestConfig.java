package kr.co.deepple.common.config;

import kr.co.deepple.common.infra.s3.S3Uploader;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistrar;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@TestConfiguration(proxyBeanMethods = false)
@Import(S3Uploader.class)
public class S3UploaderTestConfig {

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .region(Region.AP_NORTHEAST_2)
        .credentialsProvider(AnonymousCredentialsProvider.create())
        .build();
  }

  @Bean
  public DynamicPropertyRegistrar s3UploaderProperties() {
    return registry -> registry.add("spring.cloud.aws.s3.bucket", () -> S3ContainerConfig.BUCKET);
  }
}
