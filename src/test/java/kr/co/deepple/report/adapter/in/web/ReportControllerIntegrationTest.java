package kr.co.deepple.report.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.report.application.port.out.ReportRepository;
import kr.co.deepple.report.domain.Report;
import kr.co.deepple.report.domain.ReportErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class ReportControllerIntegrationTest extends IntegrationTestSupport {

  private static final int MAX_CONTENT_LENGTH = 1000;

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Autowired private MemberRepository memberRepository;

  @Autowired private ReportRepository reportRepository;

  private String accessToken;
  private long reporteeId;

  @BeforeEach
  void setUp() {
    long reporterId = memberRepository.save(MemberFixture.builder().build()).getId();
    reporteeId = memberRepository.save(MemberFixture.builder().build()).getId();
    accessToken = tokenProvider.createAccessToken(reporterId, Role.MEMBER, Instant.now());
  }

  @Test
  @DisplayName("신고 내용이 1,000자면 그대로 저장한다.")
  void saveReportWithMaxLengthContent() {
    // given
    String content = "가".repeat(MAX_CONTENT_LENGTH);

    // when
    MvcTestResult result = report(content);

    // then
    assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    assertThat(reportRepository.findAll())
        .singleElement()
        .extracting(Report::getContent)
        .isEqualTo(content);
  }

  @Test
  @DisplayName("신고 내용이 1,000자를 넘으면 400으로 응답하고 저장하지 않는다.")
  void respondBadRequestWhenContentIsTooLong() {
    // when
    MvcTestResult result = report("가".repeat(MAX_CONTENT_LENGTH + 1));

    // then
    assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(result).bodyJson().extractingPath("$.errors[0].field").isEqualTo("content");
    assertThat(reportRepository.count()).isZero();
  }

  @Test
  @DisplayName("신고 내용이 빈 문자열이어도 저장한다.")
  void saveReportWithEmptyContent() {
    // when
    MvcTestResult result = report("");

    // then
    assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    assertThat(reportRepository.count()).isEqualTo(1L);
  }

  @Test
  @DisplayName("같은 대상을 다시 신고하면 409로 응답한다.")
  void respondConflictWhenReportAlreadyExists() {
    // given
    report("첫 신고");

    // when
    MvcTestResult result = report("다시 신고");

    // then
    assertThat(result).hasStatus(HttpStatus.CONFLICT);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.code")
        .isEqualTo(ReportErrorCode.REPORT_ALREADY_EXISTS.name());
    assertThat(reportRepository.count()).isEqualTo(1L);
  }

  private MvcTestResult report(String content) {
    return mockMvc
        .post()
        .uri("/reports")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            """
            {"reporteeId": %d, "targetType": "MEMBER", "targetId": %d, "reason": "ETC", "content": "%s"}
            """
                .formatted(reporteeId, reporteeId, content))
        .exchange();
  }
}
