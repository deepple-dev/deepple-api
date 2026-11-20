package kr.co.deepple.image.adapter.out.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class S3PresignedUrlGeneratorTest {

  private S3Presigner s3Presigner;
  private S3PresignedUrlGenerator generator;

  @BeforeEach
  void setUp() {
    s3Presigner =
        S3Presigner.builder()
            .region(Region.AP_NORTHEAST_2)
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create("access", "secret")))
            .build();
    generator = new S3PresignedUrlGenerator(s3Presigner);
    ReflectionTestUtils.setField(generator, "bucket", "deepple-test");
  }

  @AfterEach
  void tearDown() {
    s3Presigner.close();
  }

  @Test
  @DisplayName("발급한 key로 10분 동안 쓸 수 있고 형식과 크기가 서명에 들어간 PUT URL을 만든다.")
  void generate() {
    URI uri = URI.create(generator.generate("tmp/members/1/abc", "image/webp", 1024L));

    assertThat(uri.getHost()).isEqualTo("deepple-test.s3.ap-northeast-2.amazonaws.com");
    assertThat(uri.getPath()).isEqualTo("/tmp/members/1/abc");
    assertThat(uri.getQuery())
        .contains("X-Amz-SignedHeaders=content-length;content-type;host")
        .contains("X-Amz-Expires=600");
  }
}
