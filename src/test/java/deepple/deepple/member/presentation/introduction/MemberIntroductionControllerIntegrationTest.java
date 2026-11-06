package deepple.deepple.member.presentation.introduction;

import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.common.enums.Role;
import deepple.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MemberIntroductionControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("유형별 이상형을 연 적이 없으면 값이 비어 있는 오픈 상태를 응답한다.")
    void respondEmptyOpenStatus() {
        MvcTestResult result = mockMvc.get()
            .uri("/member/introduction/personality-status")
            .header(HttpHeaders.AUTHORIZATION,
                "Bearer " + tokenProvider.createAccessToken(Long.MAX_VALUE, Role.MEMBER, Instant.now()))
            .exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.type").isNull();
        assertThat(result).bodyJson().extractingPath("$.openedAt").isNull();
    }
}
