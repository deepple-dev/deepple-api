package kr.co.deepple.interview.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import kr.co.deepple.interview.adapter.out.persistence.InterviewAnswerQueryRepository;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswer;
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
}
