package kr.co.deepple.datingexam.application.port.out;

import java.util.Set;
import kr.co.deepple.datingexam.domain.DatingExamSubmit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatingExamSubmitRepository extends JpaRepository<DatingExamSubmit, Long> {
  boolean existsByMemberIdAndSubjectId(long memberId, long subjectId);

  Set<DatingExamSubmit> findAllByMemberId(long memberId);
}
