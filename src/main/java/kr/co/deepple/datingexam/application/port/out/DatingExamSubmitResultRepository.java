package kr.co.deepple.datingexam.application.port.out;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.co.deepple.datingexam.domain.DatingExamSubmitResult;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface DatingExamSubmitResultRepository extends Repository<DatingExamSubmitResult, Long> {
  Optional<DatingExamSubmitResult> findByMemberId(long memberId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT r FROM DatingExamSubmitResult r WHERE r.memberId = :memberId")
  Optional<DatingExamSubmitResult> findByMemberIdForUpdate(@Param("memberId") long memberId);

  DatingExamSubmitResult save(DatingExamSubmitResult result);
}
