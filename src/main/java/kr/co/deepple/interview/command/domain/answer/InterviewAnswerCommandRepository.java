package kr.co.deepple.interview.command.domain.answer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewAnswerCommandRepository extends JpaRepository<InterviewAnswer, Long> {
  boolean existsByMemberId(long memberId);

  boolean existsByQuestionIdAndMemberId(long questionId, long memberId);
}
