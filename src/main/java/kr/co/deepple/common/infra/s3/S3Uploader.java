package kr.co.deepple.common.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

@Component
@RequiredArgsConstructor
public class S3Uploader {

  private final S3Client s3Client;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${storage.public-url:}")
  private String publicUrl;

  public String getObjectUrl(String key) {
    if (key == null) {
      return null;
    }
    if (!publicUrl.isBlank()) {
      return publicUrl + "/" + key;
    }
    return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toString();
  }
}
