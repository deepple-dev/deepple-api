package kr.co.deepple.common.infra.google;

import com.google.auth.oauth2.AwsSecurityCredentials;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AwsSdkCredentialsSupplier 단위 테스트")
class AwsSdkCredentialsSupplierTest {

    @Test
    @DisplayName("AWS 임시 자격 증명을 세션 토큰과 함께 넘긴다")
    void passesSessionCredentials() {
        AwsSdkCredentialsSupplier supplier = new AwsSdkCredentialsSupplier(
            StaticCredentialsProvider.create(AwsSessionCredentials.create("access", "secret", "token")),
            "ap-northeast-2"
        );

        AwsSecurityCredentials credentials = supplier.getCredentials(null);

        assertThat(credentials.getAccessKeyId()).isEqualTo("access");
        assertThat(credentials.getSecretAccessKey()).isEqualTo("secret");
        assertThat(credentials.getSessionToken()).isEqualTo("token");
        assertThat(supplier.getRegion(null)).isEqualTo("ap-northeast-2");
    }

    @Test
    @DisplayName("세션 토큰이 없는 자격 증명이면 토큰을 비운다")
    void passesBasicCredentials() {
        AwsSdkCredentialsSupplier supplier = new AwsSdkCredentialsSupplier(
            StaticCredentialsProvider.create(AwsBasicCredentials.create("access", "secret")),
            "ap-northeast-2"
        );

        assertThat(supplier.getCredentials(null).getSessionToken()).isNull();
    }
}
