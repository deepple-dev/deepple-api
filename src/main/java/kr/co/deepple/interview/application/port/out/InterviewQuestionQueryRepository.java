package kr.co.deepple.interview.application.port.out;

import java.util.List;
import java.util.Optional;
import kr.co.deepple.interview.application.dto.InterviewQuestionSearchCondition;
import kr.co.deepple.interview.application.dto.InterviewQuestionView;

public interface InterviewQuestionQueryRepository {
  List<InterviewQuestionView> findAllQuestionByCategoryWithMemberId(
      InterviewQuestionSearchCondition condition, long memberId);

  Optional<InterviewQuestionView> findQuestionByIdWithMemberId(long questionId, long memberId);
}
