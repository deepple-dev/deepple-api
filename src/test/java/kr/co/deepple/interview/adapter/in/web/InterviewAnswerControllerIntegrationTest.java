package kr.co.deepple.interview.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.interview.application.port.out.InterviewAnswerRepository;
import kr.co.deepple.interview.application.port.out.InterviewQuestionRepository;
import kr.co.deepple.interview.domain.InterviewAnswer;
import kr.co.deepple.interview.domain.InterviewQuestion;
import kr.co.deepple.interview.fixture.InterviewAnswerFixture;
import kr.co.deepple.member.application.port.out.MemberRepository;
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

class InterviewAnswerControllerIntegrationTest extends IntegrationTestSupport {

  private static final int MAX_CONTENT_LENGTH = 1000;

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Autowired private MemberRepository memberRepository;

  @Autowired private InterviewQuestionRepository interviewQuestionRepository;

  @Autowired private InterviewAnswerRepository interviewAnswerRepository;

  private long memberId;
  private String accessToken;
  private long questionId;

  @BeforeEach
  void setUp() {
    memberId = memberRepository.save(MemberFixture.builder().build()).getId();
    accessToken = tokenProvider.createAccessToken(memberId, Role.MEMBER, Instant.now());
    questionId =
        interviewQuestionRepository.findAll().stream()
            .filter(InterviewQuestion::isPublic)
            .findFirst()
            .orElseThrow()
            .getId();
  }

  @Test
  @DisplayName("답변이 1,000자면 그대로 등록한다.")
  void saveAnswerWithMaxLengthContent() {
    // given
    String content = "가".repeat(MAX_CONTENT_LENGTH);

    // when
    MvcTestResult result = save(content);

    // then
    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(interviewAnswerRepository.findAll())
        .singleElement()
        .extracting(InterviewAnswer::getContent)
        .isEqualTo(content);
  }

  @Test
  @DisplayName("답변이 1,000자를 넘으면 400으로 응답하고 등록하지 않는다.")
  void respondBadRequestWhenSavedContentIsTooLong() {
    // when
    MvcTestResult result = save("가".repeat(MAX_CONTENT_LENGTH + 1));

    // then
    assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(result).bodyJson().extractingPath("$.errors[0].field").isEqualTo("answerContent");
    assertThat(interviewAnswerRepository.count()).isZero();
  }

  @Test
  @DisplayName("답변을 1,000자로 수정하면 그대로 저장한다.")
  void updateAnswerWithMaxLengthContent() {
    // given
    long answerId = saveAnswer();
    String content = "가".repeat(MAX_CONTENT_LENGTH);

    // when
    MvcTestResult result = update(answerId, content);

    // then
    assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    assertThat(interviewAnswerRepository.findById(answerId))
        .get()
        .extracting(InterviewAnswer::getContent)
        .isEqualTo(content);
  }

  @Test
  @DisplayName("답변을 1,000자보다 길게 수정하면 400으로 응답하고 바꾸지 않는다.")
  void respondBadRequestWhenUpdatedContentIsTooLong() {
    // given
    long answerId = saveAnswer();

    // when
    MvcTestResult result = update(answerId, "가".repeat(MAX_CONTENT_LENGTH + 1));

    // then
    assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(result).bodyJson().extractingPath("$.errors[0].field").isEqualTo("answerContent");
    assertThat(interviewAnswerRepository.findById(answerId))
        .get()
        .extracting(InterviewAnswer::getContent)
        .isEqualTo("인터뷰 답변");
  }

  private long saveAnswer() {
    return interviewAnswerRepository
        .save(
            InterviewAnswerFixture.builder()
                .questionId(questionId)
                .memberId(memberId)
                .content("인터뷰 답변")
                .build())
        .getId();
  }

  private MvcTestResult save(String content) {
    return mockMvc
        .post()
        .uri("/interview/answer")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            """
            {"interviewQuestionId": %d, "answerContent": "%s"}
            """
                .formatted(questionId, content))
        .exchange();
  }

  private MvcTestResult update(long answerId, String content) {
    return mockMvc
        .patch()
        .uri("/interview/answer/{id}", answerId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            """
            {"answerContent": "%s"}
            """
                .formatted(content))
        .exchange();
  }
}
