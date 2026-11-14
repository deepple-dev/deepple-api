package kr.co.deepple.interview.application.port.out;

import java.util.List;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswer;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswerDetail;

public interface InterviewAnswerQueryRepository {
  List<MemberInterviewAnswer> findPublicAnswersByMemberId(long memberId);

  List<MemberInterviewAnswerDetail> findAllAnswersByMemberId(long memberId);
}
