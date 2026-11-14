package kr.co.deepple.interview.application;

import java.util.List;
import kr.co.deepple.interview.adapter.out.persistence.InterviewAnswerQueryRepository;
import kr.co.deepple.interview.application.port.in.FindMemberInterviewAnswersUseCase;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswer;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswerDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindMemberInterviewAnswersService implements FindMemberInterviewAnswersUseCase {

  private final InterviewAnswerQueryRepository interviewAnswerQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public List<MemberInterviewAnswer> findPublicInterviewAnswers(long memberId) {
    return interviewAnswerQueryRepository.findPublicAnswersByMemberId(memberId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MemberInterviewAnswerDetail> findAllInterviewAnswers(long memberId) {
    return interviewAnswerQueryRepository.findAllAnswersByMemberId(memberId);
  }
}
