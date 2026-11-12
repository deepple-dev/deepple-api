package kr.co.deepple.community.presentation.selfintroduction;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.auth.domain.TokenProvider;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class SelfIntroductionControllerIntegrationTest extends IntegrationTestSupport {

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Test
  @DisplayName("없는 셀프 소개를 조회하면 404와 SELF_INTRODUCTION_NOT_FOUND를 응답한다.")
  void respondNotFound() {
    MvcTestResult result =
        mockMvc
            .get()
            .uri("/self-introduction/{id}", Long.MAX_VALUE)
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + tokenProvider.createAccessToken(1L, Role.MEMBER, Instant.now()))
            .exchange();

    assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("SELF_INTRODUCTION_NOT_FOUND");
  }
}
