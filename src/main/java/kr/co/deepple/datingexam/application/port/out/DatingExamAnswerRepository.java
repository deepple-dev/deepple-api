package kr.co.deepple.datingexam.application.port.out;

import java.util.Collection;
import java.util.List;
import kr.co.deepple.datingexam.domain.DatingExamAnswer;
import org.springframework.data.repository.Repository;

public interface DatingExamAnswerRepository extends Repository<DatingExamAnswer, Long> {
  List<DatingExamAnswer> findAllByIdIn(Collection<Long> ids);
}
