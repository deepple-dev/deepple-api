package kr.co.deepple.common.infra.s3;

import static kr.co.deepple.common.config.S3ContainerConfig.BUCKET;
import static org.assertj.core.api.Assertions.assertThat;

import io.awspring.cloud.s3.S3Template;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import kr.co.deepple.common.infra.s3.dto.PresignedUrlResponse;
import kr.co.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("S3Uploader 통합 테스트")
class S3UploaderIntegrationTest extends IntegrationTestSupport {

  @Autowired private S3Uploader s3Uploader;

  @Autowired private S3Template s3Template;

  @Test
  @DisplayName("presigned URL로 올린 파일이 객체 URL 위치에 저장된다")
  void uploadWithPresignedUrl() throws Exception {
    PresignedUrlResponse response = s3Uploader.getPresignedUrl("a.png", 1L, "profile");

    HttpRequest request =
        HttpRequest.newBuilder(URI.create(response.presignedUrl()))
            .PUT(HttpRequest.BodyPublishers.ofByteArray(new byte[] {1, 2, 3}))
            .build();
    HttpResponse<Void> uploadResponse =
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());

    String bucketPath = BUCKET + "/";
    String key =
        response
            .objectUrl()
            .substring(response.objectUrl().indexOf(bucketPath) + bucketPath.length());
    assertThat(uploadResponse.statusCode()).isEqualTo(200);
    assertThat(s3Template.objectExists(BUCKET, key)).isTrue();
  }
}
