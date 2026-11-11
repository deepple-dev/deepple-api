package kr.co.deepple.datingexam.application.required;

import kr.co.deepple.datingexam.domain.DatingExamSubject;
import kr.co.deepple.datingexam.domain.SubjectType;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.Set;

public interface DatingExamSubjectRepository extends Repository<DatingExamSubject, Long> {
    Set<DatingExamSubject> findAllByType(SubjectType type);

    Optional<DatingExamSubject> findById(Long id);
}
