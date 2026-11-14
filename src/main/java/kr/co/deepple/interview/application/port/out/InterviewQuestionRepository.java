package kr.co.deepple.interview.application.port.out;

import kr.co.deepple.interview.domain.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
  boolean existsByContent(String content);
}
