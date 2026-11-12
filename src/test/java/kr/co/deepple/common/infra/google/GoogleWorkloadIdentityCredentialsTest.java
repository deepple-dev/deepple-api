package kr.co.deepple.common.infra.google;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.auth.oauth2.AwsCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;

@DisplayName("GoogleWorkloadIdentityCredentials 단위 테스트")
class GoogleWorkloadIdentityCredentialsTest {

  private static final String AUDIENCE =
      "//iam.googleapis.com/projects/123/locations/global/workloadIdentityPools/aws/providers/deepple";

  @Test
  @DisplayName("공급자와 가장할 서비스 계정으로 AWS 기반 자격 증명을 만든다")
  void createsAwsCredentials() {
    GoogleCredentials credentials =
        GoogleWorkloadIdentityCredentials.create(
            AUDIENCE,
            "app@project.iam.gserviceaccount.com",
            AnonymousCredentialsProvider.create(),
            Region.AP_NORTHEAST_2);

    assertThat(credentials).isInstanceOf(AwsCredentials.class);
    AwsCredentials awsCredentials = (AwsCredentials) credentials;
    assertThat(awsCredentials.getAudience()).isEqualTo(AUDIENCE);
    assertThat(awsCredentials.getServiceAccountEmail())
        .isEqualTo("app@project.iam.gserviceaccount.com");
    assertThat(awsCredentials.getTokenUrl()).isEqualTo("https://sts.googleapis.com/v1/token");
  }
}
