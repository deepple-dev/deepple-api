package deepple.deepple.payment.command.infra.order;

import deepple.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("App Store 인증서 OCSP 타임아웃 통합 테스트")
class AppStoreOcspTimeoutIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("영수증 검증기가 만들어지면 OCSP 타임아웃이 3초로 설정된다")
    void setOcspTimeoutWhenVerifierCreated() {
        assertThat(System.getProperty("com.sun.security.ocsp.timeout")).isEqualTo("3");
    }
}
