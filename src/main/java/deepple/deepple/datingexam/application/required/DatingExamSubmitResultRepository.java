package deepple.deepple.datingexam.application.required;

import deepple.deepple.datingexam.domain.DatingExamSubmitResult;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DatingExamSubmitResultRepository extends Repository<DatingExamSubmitResult, Long> {
    Optional<DatingExamSubmitResult> findByMemberId(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM DatingExamSubmitResult r WHERE r.memberId = :memberId")
    Optional<DatingExamSubmitResult> findByMemberIdForUpdate(@Param("memberId") Long memberId);

    DatingExamSubmitResult save(DatingExamSubmitResult result);
}
