package kr.co.deepple.common.infra.google;

import com.google.auth.oauth2.AwsCredentials;
import com.google.auth.oauth2.ExternalAccountCredentials.SubjectTokenTypes;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GoogleWorkloadIdentityCredentials {

  private static final String IMPERSONATION_URL =
      "https://iamcredentials.googleapis.com/v1/projects/-/serviceAccounts/%s:generateAccessToken";

  public static GoogleCredentials create(
      String audience,
      String serviceAccount,
      AwsCredentialsProvider awsCredentialsProvider,
      Region region) {
    return AwsCredentials.newBuilder()
        .setAudience(audience)
        .setSubjectTokenType(SubjectTokenTypes.AWS4)
        .setServiceAccountImpersonationUrl(IMPERSONATION_URL.formatted(serviceAccount))
        .setAwsSecurityCredentialsSupplier(
            new AwsSdkCredentialsSupplier(awsCredentialsProvider, region.id()))
        .build();
  }
}
