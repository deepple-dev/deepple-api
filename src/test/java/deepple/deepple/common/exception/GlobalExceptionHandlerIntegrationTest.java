package deepple.deepple.common.exception;

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

class GlobalExceptionHandlerIntegrationTest extends IntegrationTestSupport {

    private static final String LOGIN_API = "/member/login";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("없는 경로는 404로 응답한다.")
    void respondNotFoundForUnknownPath() {
        MvcTestResult result = mockMvc.get()
            .uri("/unknown-path")
            .header(HttpHeaders.AUTHORIZATION, memberBearer())
            .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("NOT_FOUND");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("요청한 경로를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("지원하지 않는 메서드는 405로 응답한다.")
    void respondMethodNotAllowed() {
        MvcTestResult result = mockMvc.delete()
            .uri("/member/profile/contact")
            .header(HttpHeaders.AUTHORIZATION, memberBearer())
            .exchange();

        assertThat(result).hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(result).headers().containsHeader(HttpHeaders.ALLOW);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("METHOD_NOT_ALLOWED");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("지원하지 않는 HTTP 메서드입니다.");
    }

    @Test
    @DisplayName("본문이 없으면 400과 고정 메시지로 응답한다.")
    void respondMissingBody() {
        MvcTestResult result = mockMvc.post()
            .uri(LOGIN_API)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("BAD_REQUEST");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("요청 본문을 읽을 수 없습니다.");
        assertThat(result).bodyText().doesNotContain("MemberAuthController");
    }

    @Test
    @DisplayName("본문에 알 수 없는 필드가 있으면 400과 필드 이름을 응답한다.")
    void respondUnknownField() {
        MvcTestResult result = mockMvc.post()
            .uri(LOGIN_API)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"phoneNumber\":\"01012345678\",\"code\":\"123456\",\"unknown\":\"value\"}")
            .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("알 수 없는 필드입니다: unknown");
    }

    private String memberBearer() {
        return "Bearer " + tokenProvider.createAccessToken(1L, Role.MEMBER, Instant.now());
    }
}
