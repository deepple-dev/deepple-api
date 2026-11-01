package deepple.deepple.common.infra.s3;

import deepple.deepple.common.infra.s3.dto.PresignedUrlResponse;
import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("S3Uploader 단위 테스트")
class S3UploaderTest {

    private static final String BUCKET = "deepple-test";

    private final S3Template s3Template = mock(S3Template.class);

    private S3Uploader createUploader(S3Client s3Client) throws Exception {
        when(s3Template.createSignedPutURL(eq(BUCKET), any(), any()))
            .thenReturn(URI.create("https://presigned.example.com").toURL());
        S3Uploader uploader = new S3Uploader(s3Template, s3Client);
        ReflectionTestUtils.setField(uploader, "bucket", BUCKET);
        return uploader;
    }

    @Test
    @DisplayName("AWS 엔드포인트면 버킷 가상 호스트 주소를 돌려준다")
    void objectUrlOnAws() throws Exception {
        S3Client s3Client = S3Client.builder()
            .region(Region.AP_NORTHEAST_2)
            .build();

        PresignedUrlResponse response = createUploader(s3Client).getPresignedUrl("a.png", 1L);

        assertThat(response.objectUrl())
            .startsWith("https://" + BUCKET + ".s3.ap-northeast-2.amazonaws.com/1/")
            .endsWith(".png");
    }

    @Test
    @DisplayName("엔드포인트를 바꾸면 그 주소를 경로 방식으로 돌려준다")
    void objectUrlOnCustomEndpoint() throws Exception {
        S3Client s3Client = S3Client.builder()
            .region(Region.AP_NORTHEAST_2)
            .endpointOverride(URI.create("http://localhost:9090"))
            .forcePathStyle(true)
            .build();

        PresignedUrlResponse response = createUploader(s3Client).getPresignedUrl("a.png", 1L, "profile");

        assertThat(response.objectUrl())
            .startsWith("http://localhost:9090/" + BUCKET + "/profile/1/")
            .endsWith(".png");
    }
}
