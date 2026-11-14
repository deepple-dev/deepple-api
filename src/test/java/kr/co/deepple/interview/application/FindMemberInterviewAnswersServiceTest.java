package kr.co.deepple.interview.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswer;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswerDetail;
import kr.co.deepple.interview.application.port.out.InterviewAnswerQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindMemberInterviewAnswersServiceTest {

  @Mock private InterviewAnswerQueryRepository interviewAnswerQueryRepository;

  @InjectMocks private FindMemberInterviewAnswersService findMemberInterviewAnswersService;

  @Test
  @DisplayName("회원이 공개 질문에 남긴 답변을 돌려준다.")
  void findPublicInterviewAnswers() {
    MemberInterviewAnswer answer = new MemberInterviewAnswer(1L, "질문", "PERSONAL", 10L, "답변");
    given(interviewAnswerQueryRepository.findPublicAnswersByMemberId(7L))
        .willReturn(List.of(answer));

    assertThat(findMemberInterviewAnswersService.findPublicInterviewAnswers(7L))
        .containsExactly(answer);
  }

  @Test
  @DisplayName("회원이 남긴 모든 답변을 돌려준다.")
  void findAllInterviewAnswers() {
    MemberInterviewAnswerDetail publicAnswer =
        new MemberInterviewAnswerDetail(10L, 1L, "질문", true, "답변");
    MemberInterviewAnswerDetail privateAnswer =
        new MemberInterviewAnswerDetail(11L, 2L, "비공개 질문", false, "답변2");
    given(interviewAnswerQueryRepository.findAllAnswersByMemberId(7L))
        .willReturn(List.of(publicAnswer, privateAnswer));

    assertThat(findMemberInterviewAnswersService.findAllInterviewAnswers(7L))
        .containsExactly(publicAnswer, privateAnswer);
  }
}
