package deepple.deepple.member.presentation.member;

import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.common.enums.Role;
import deepple.deepple.common.support.IntegrationTestSupport;
import deepple.deepple.notification.command.domain.DeviceRegistration;
import deepple.deepple.notification.command.domain.DeviceRegistrationCommandRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class MemberLogoutIntegrationTest extends IntegrationTestSupport {

    private static final long MEMBER_ID = 9001L;

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private DeviceRegistrationCommandRepository deviceRegistrationCommandRepository;

    @AfterEach
    void tearDown() {
        deviceRegistrationCommandRepository.deleteAll();
    }

    @Test
    @DisplayName("로그아웃하면 회원의 기기 등록이 비활성화된다.")
    void deactivateDeviceRegistrationOnLogout() {
        deviceRegistrationCommandRepository.save(DeviceRegistration.of(MEMBER_ID, "device-id", "fcm-token"));
        String accessToken = tokenProvider.createAccessToken(MEMBER_ID, Role.MEMBER, Instant.now());

        MvcTestResult result = mockMvc.get()
            .uri("/member/logout")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();

        assertThat(result).hasStatusOk();
        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> assertThat(deviceRegistrationCommandRepository.findByMemberIdAndIsActiveTrue(MEMBER_ID))
                .isEmpty());
    }
}
