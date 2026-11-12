package kr.co.deepple.datingexam.application.port.out;

import java.util.Optional;
import java.util.Set;
import kr.co.deepple.datingexam.domain.DatingExamSubject;
import kr.co.deepple.datingexam.domain.SubjectType;
import org.springframework.data.repository.Repository;

public interface DatingExamSubjectRepository extends Repository<DatingExamSubject, Long> {
  Set<DatingExamSubject> findAllByType(SubjectType type);

  Optional<DatingExamSubject> findById(Long id);
}
