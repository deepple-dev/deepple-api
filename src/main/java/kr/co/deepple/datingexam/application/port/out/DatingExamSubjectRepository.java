package kr.co.deepple.datingexam.application.port.out;

import java.util.Set;
import kr.co.deepple.datingexam.domain.DatingExamSubject;
import kr.co.deepple.datingexam.domain.SubjectType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatingExamSubjectRepository extends JpaRepository<DatingExamSubject, Long> {
  Set<DatingExamSubject> findAllByType(SubjectType type);
}
