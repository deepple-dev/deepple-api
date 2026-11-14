package kr.co.deepple.interview.application.port.out;

import kr.co.deepple.interview.domain.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewAnswerCommandRepository extends JpaRepository<InterviewAnswer, Long> {
  boolean existsByMemberId(long memberId);

  boolean existsByQuestionIdAndMemberId(long questionId, long memberId);
}
