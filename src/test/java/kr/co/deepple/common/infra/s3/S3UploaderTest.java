package kr.co.deepple.common.infra.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@DisplayName("S3Uploader 단위 테스트")
class S3UploaderTest {

  private static final String BUCKET = "deepple-test";

  private S3Uploader createUploader(S3Client s3Client, String publicUrl) {
    S3Uploader uploader = new S3Uploader(s3Client);
    ReflectionTestUtils.setField(uploader, "bucket", BUCKET);
    ReflectionTestUtils.setField(uploader, "publicUrl", publicUrl);
    return uploader;
  }

  @Test
  @DisplayName("AWS 엔드포인트면 버킷 가상 호스트 주소를 돌려준다")
  void objectUrlOnAws() {
    S3Client s3Client = S3Client.builder().region(Region.AP_NORTHEAST_2).build();

    String objectUrl = createUploader(s3Client, "").getObjectUrl("profile/1/a.webp");

    assertThat(objectUrl)
        .isEqualTo("https://" + BUCKET + ".s3.ap-northeast-2.amazonaws.com/profile/1/a.webp");
  }

  @Test
  @DisplayName("엔드포인트를 바꾸면 그 주소를 경로 방식으로 돌려준다")
  void objectUrlOnCustomEndpoint() {
    S3Client s3Client =
        S3Client.builder()
            .region(Region.AP_NORTHEAST_2)
            .endpointOverride(URI.create("http://localhost:9090"))
            .forcePathStyle(true)
            .build();

    String objectUrl = createUploader(s3Client, "").getObjectUrl("profile/1/a.webp");

    assertThat(objectUrl).isEqualTo("http://localhost:9090/" + BUCKET + "/profile/1/a.webp");
  }

  @Test
  @DisplayName("공개 주소를 정하면 S3 주소 대신 그 주소로 돌려준다")
  void objectUrlWithPublicUrl() {
    S3Client s3Client = S3Client.builder().region(Region.AP_NORTHEAST_2).build();

    String objectUrl =
        createUploader(s3Client, "https://images.example.com").getObjectUrl("profile/1/a.webp");

    assertThat(objectUrl).isEqualTo("https://images.example.com/profile/1/a.webp");
  }

  @Test
  @DisplayName("key가 없으면 null을 돌려준다")
  void objectUrlWithoutKey() {
    S3Client s3Client = S3Client.builder().region(Region.AP_NORTHEAST_2).build();

    String objectUrl = createUploader(s3Client, "").getObjectUrl(null);

    assertThat(objectUrl).isNull();
  }
}
