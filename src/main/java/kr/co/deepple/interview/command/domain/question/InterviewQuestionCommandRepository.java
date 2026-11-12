package kr.co.deepple.interview.command.domain.question;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewQuestionCommandRepository extends JpaRepository<InterviewQuestion, Long> {
    boolean existsByContent(String content);
}
