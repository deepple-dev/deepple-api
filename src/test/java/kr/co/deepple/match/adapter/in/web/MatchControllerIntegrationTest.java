package kr.co.deepple.match.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class MatchControllerIntegrationTest extends IntegrationTestSupport {

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Autowired private MemberRepository memberRepository;

  private String accessToken;

  @BeforeEach
  void setUp() {
    Member member = memberRepository.save(MemberFixture.builder().build());
    accessToken = tokenProvider.createAccessToken(member.getId(), Role.MEMBER, Instant.now());
  }

  @Test
  @DisplayName("연락처 타입 없이 메시지를 보내면 400으로 응답한다.")
  void respondBadRequestWhenRequestWithoutContactType() {
    MvcTestResult result =
        mockMvc
            .post()
            .uri("/match/request")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"responderId": 1, "requestMessage": "안녕하세요"}
                """)
            .exchange();

    assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(result).bodyJson().extractingPath("$.errors[0].field").isEqualTo("contactType");
  }

  @Test
  @DisplayName("연락처 타입 없이 매칭을 수락하면 400으로 응답한다.")
  void respondBadRequestWhenApproveWithoutContactType() {
    MvcTestResult result =
        mockMvc
            .patch()
            .uri("/match/{matchId}/approve", 1L)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"responseMessage": "반가워요"}
                """)
            .exchange();

    assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(result).bodyJson().extractingPath("$.errors[0].field").isEqualTo("contactType");
  }
}
