package kr.co.deepple.interview.application.port.in;

import java.util.List;

public interface FindMemberInterviewAnswersUseCase {
  List<MemberInterviewAnswer> findPublicInterviewAnswers(long memberId);

  List<MemberInterviewAnswerDetail> findAllInterviewAnswers(long memberId);
}
