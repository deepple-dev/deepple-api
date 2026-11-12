package kr.co.deepple.interview.command.domain.answer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewAnswerCommandRepository extends JpaRepository<InterviewAnswer, Long> {
  boolean existsByMemberId(Long memberId);

  boolean existsByQuestionIdAndMemberId(Long questionId, Long memberId);
}
