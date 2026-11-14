package kr.co.deepple.interview.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswer;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswerDetail;
import kr.co.deepple.interview.domain.InterviewAnswer;
import kr.co.deepple.interview.domain.InterviewCategory;
import kr.co.deepple.interview.domain.InterviewQuestion;
import kr.co.deepple.interview.fixture.InterviewAnswerFixture;
import kr.co.deepple.interview.fixture.InterviewQuestionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class InterviewAnswerQueryRepositoryTest extends RepositoryTestSupport {

  @Autowired private TestEntityManager em;

  @Autowired private InterviewAnswerQueryRepository interviewAnswerQueryRepository;

  @Test
  @DisplayName("회원이 공개 질문에 남긴 답변만 답변한 순서대로 조회한다.")
  void findPublicAnswersByMemberId() {
    long memberId = 100L;
    long otherMemberId = 200L;
    InterviewQuestion publicQuestion1 = persistQuestion("question1", true);
    InterviewQuestion privateQuestion = persistQuestion("question2", false);
    InterviewQuestion publicQuestion2 = persistQuestion("question3", true);
    persistQuestion("question4", true);

    InterviewAnswer answer1 = persistAnswer(publicQuestion1, memberId, "answer for question1");
    persistAnswer(privateQuestion, memberId, "answer for question2");
    InterviewAnswer answer3 = persistAnswer(publicQuestion2, memberId, "answer for question3");
    persistAnswer(publicQuestion1, otherMemberId, "other answer");
    em.flush();
    em.clear();

    List<MemberInterviewAnswer> answers =
        interviewAnswerQueryRepository.findPublicAnswersByMemberId(memberId);

    assertThat(answers)
        .containsExactly(
            new MemberInterviewAnswer(
                publicQuestion1.getId(),
                "question1",
                "PERSONAL",
                answer1.getId(),
                "answer for question1"),
            new MemberInterviewAnswer(
                publicQuestion2.getId(),
                "question3",
                "PERSONAL",
                answer3.getId(),
                "answer for question3"));
  }

  @Test
  @DisplayName("답변이 없는 회원은 빈 목록을 돌려준다.")
  void returnEmptyWhenNoAnswers() {
    persistQuestion("question1", true);
    em.flush();
    em.clear();

    assertThat(interviewAnswerQueryRepository.findPublicAnswersByMemberId(100L)).isEmpty();
  }

  @Test
  @DisplayName("회원의 모든 답변을 질문 공개 여부와 관계없이 답변한 순서대로 조회하고, 질문이 없는 답변은 질문 값이 비어 있다.")
  void findAllAnswersByMemberId() {
    long memberId = 100L;
    long otherMemberId = 200L;
    InterviewQuestion publicQuestion = persistQuestion("question1", true);
    InterviewQuestion privateQuestion = persistQuestion("question2", false);
    persistQuestion("question3", true);

    InterviewAnswer answer1 = persistAnswer(publicQuestion, memberId, "answer for question1");
    InterviewAnswer answer2 = persistAnswer(privateQuestion, memberId, "answer for question2");
    InterviewAnswer answer3 =
        em.persist(
            InterviewAnswerFixture.builder()
                .questionId(Long.MAX_VALUE)
                .memberId(memberId)
                .content("answer for missing question")
                .build());
    InterviewAnswer answer4 = persistAnswer(publicQuestion, memberId, "second answer");
    persistAnswer(publicQuestion, otherMemberId, "other answer");
    em.flush();
    em.clear();

    List<MemberInterviewAnswerDetail> answers =
        interviewAnswerQueryRepository.findAllAnswersByMemberId(memberId);

    assertThat(answers)
        .containsExactly(
            new MemberInterviewAnswerDetail(
                answer1.getId(), publicQuestion.getId(), "question1", true, "answer for question1"),
            new MemberInterviewAnswerDetail(
                answer2.getId(),
                privateQuestion.getId(),
                "question2",
                false,
                "answer for question2"),
            new MemberInterviewAnswerDetail(
                answer3.getId(), null, null, false, "answer for missing question"),
            new MemberInterviewAnswerDetail(
                answer4.getId(), publicQuestion.getId(), "question1", true, "second answer"));
  }

  @Test
  @DisplayName("답변이 없는 회원의 모든 답변 조회는 빈 목록을 돌려준다.")
  void returnEmptyWhenNoAnswersForAllAnswers() {
    persistQuestion("question1", true);
    em.flush();
    em.clear();

    assertThat(interviewAnswerQueryRepository.findAllAnswersByMemberId(100L)).isEmpty();
  }

  private InterviewQuestion persistQuestion(String content, boolean isPublic) {
    return em.persist(
        InterviewQuestionFixture.builder()
            .content(content)
            .category(InterviewCategory.PERSONAL)
            .publicQuestion(isPublic)
            .build());
  }

  private InterviewAnswer persistAnswer(InterviewQuestion question, long memberId, String content) {
    return em.persist(
        InterviewAnswerFixture.builder()
            .questionId(question.getId())
            .memberId(memberId)
            .content(content)
            .build());
  }
}
