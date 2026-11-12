package kr.co.deepple.common.infra.s3;

import io.awspring.cloud.s3.S3Template;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import kr.co.deepple.common.infra.s3.dto.PresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

@Component
public class S3Uploader {

  private static final int PRESIGNED_URL_EXPIRATION_MINUTES = 10;

  private final S3Template s3Template;
  private final S3Client s3Client;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${storage.public-url:}")
  private String publicUrl;

  public S3Uploader(S3Template s3Template, S3Client s3Client) {
    this.s3Template = s3Template;
    this.s3Client = s3Client;
  }

  public PresignedUrlResponse getPresignedUrl(String fileName, Long memberId) {
    String key = generateUniqueKey(fileName, memberId);
    URL presignedUrl =
        s3Template.createSignedPutURL(
            bucket, key, Duration.ofMinutes(PRESIGNED_URL_EXPIRATION_MINUTES));
    String objectUrl = getObjectUrl(key);

    return new PresignedUrlResponse(presignedUrl.toString(), objectUrl);
  }

  public PresignedUrlResponse getPresignedUrl(String fileName, Long memberId, String pathPrefix) {
    String key = pathPrefix + "/" + generateUniqueKey(fileName, memberId);
    URL presignedUrl =
        s3Template.createSignedPutURL(
            bucket, key, Duration.ofMinutes(PRESIGNED_URL_EXPIRATION_MINUTES));
    String objectUrl = getObjectUrl(key);

    return new PresignedUrlResponse(presignedUrl.toString(), objectUrl);
  }

  private String getObjectUrl(String key) {
    if (!publicUrl.isBlank()) {
      return publicUrl + "/" + key;
    }
    return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(key)).toString();
  }

  private String generateUniqueKey(String fileName, Long memberId) {
    String extension = fileName.substring(fileName.lastIndexOf("."));
    return memberId + "/" + UUID.randomUUID() + extension;
  }
}
