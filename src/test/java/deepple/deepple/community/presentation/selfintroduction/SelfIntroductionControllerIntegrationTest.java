package deepple.deepple.community.presentation.selfintroduction;

import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.common.enums.Role;
import deepple.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SelfIntroductionControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("없는 셀프 소개를 조회하면 404와 SELF_INTRODUCTION_NOT_FOUND를 응답한다.")
    void respondNotFound() {
        MvcTestResult result = mockMvc.get()
            .uri("/self-introduction/{id}", Long.MAX_VALUE)
            .header(HttpHeaders.AUTHORIZATION,
                "Bearer " + tokenProvider.createAccessToken(1L, Role.MEMBER, Instant.now()))
            .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("SELF_INTRODUCTION_NOT_FOUND");
    }
}
