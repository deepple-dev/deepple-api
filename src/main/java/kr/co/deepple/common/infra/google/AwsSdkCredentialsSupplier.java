package kr.co.deepple.common.infra.google;

import com.google.auth.oauth2.AwsSecurityCredentials;
import com.google.auth.oauth2.AwsSecurityCredentialsSupplier;
import com.google.auth.oauth2.ExternalAccountSupplierContext;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

/** Google 라이브러리는 ECS 태스크 역할 자격 증명을 직접 읽지 못해 AWS SDK에서 받아 넘긴다. */
class AwsSdkCredentialsSupplier implements AwsSecurityCredentialsSupplier {

  private final transient AwsCredentialsProvider awsCredentialsProvider;
  private final String region;

  AwsSdkCredentialsSupplier(AwsCredentialsProvider awsCredentialsProvider, String region) {
    this.awsCredentialsProvider = awsCredentialsProvider;
    this.region = region;
  }

  @Override
  public String getRegion(ExternalAccountSupplierContext context) {
    return region;
  }

  @Override
  public AwsSecurityCredentials getCredentials(ExternalAccountSupplierContext context) {
    AwsCredentials credentials = awsCredentialsProvider.resolveCredentials();
    String sessionToken =
        credentials instanceof AwsSessionCredentials session ? session.sessionToken() : null;
    return new AwsSecurityCredentials(
        credentials.accessKeyId(), credentials.secretAccessKey(), sessionToken);
  }
}
