package kr.co.deepple.datingexam.application.port.out;

import java.util.Collection;
import java.util.List;
import kr.co.deepple.datingexam.domain.DatingExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatingExamAnswerRepository extends JpaRepository<DatingExamAnswer, Long> {
  List<DatingExamAnswer> findAllByIdIn(Collection<Long> ids);
}
