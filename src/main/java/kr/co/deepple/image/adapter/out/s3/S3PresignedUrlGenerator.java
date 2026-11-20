package kr.co.deepple.image.adapter.out.s3;

import java.time.Duration;
import kr.co.deepple.image.application.port.out.PresignedUrlGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class S3PresignedUrlGenerator implements PresignedUrlGenerator {

  private static final Duration EXPIRATION = Duration.ofMinutes(10);

  private final S3Presigner s3Presigner;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  @Override
  public String generate(String key, String contentType, long contentLength) {
    PutObjectPresignRequest request =
        PutObjectPresignRequest.builder()
            .signatureDuration(EXPIRATION)
            .putObjectRequest(object ->
                object
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength))
            .build();
    return s3Presigner.presignPutObject(request).url().toString();
  }
}
