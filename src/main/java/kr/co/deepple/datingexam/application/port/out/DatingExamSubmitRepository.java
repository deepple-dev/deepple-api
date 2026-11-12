package kr.co.deepple.datingexam.application.port.out;

import java.util.Set;
import kr.co.deepple.datingexam.domain.DatingExamSubmit;
import org.springframework.data.repository.Repository;

public interface DatingExamSubmitRepository extends Repository<DatingExamSubmit, Long> {
  DatingExamSubmit save(DatingExamSubmit datingExamSubmit);

  boolean existsByMemberIdAndSubjectId(long memberId, long subjectId);

  Set<DatingExamSubmit> findAllByMemberId(long memberId);
}
