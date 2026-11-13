package kr.co.deepple.recommendation.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class MemberIntroductionControllerIntegrationTest extends IntegrationTestSupport {

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Test
  @DisplayName("유형별 이상형을 연 적이 없으면 값이 비어 있는 오픈 상태를 응답한다.")
  void respondEmptyOpenStatus() {
    MvcTestResult result =
        mockMvc
            .get()
            .uri("/member/introduction/personality-status")
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer "
                    + tokenProvider.createAccessToken(Long.MAX_VALUE, Role.MEMBER, Instant.now()))
            .exchange();

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.type").isNull();
    assertThat(result).bodyJson().extractingPath("$.openedAt").isNull();
  }
}
