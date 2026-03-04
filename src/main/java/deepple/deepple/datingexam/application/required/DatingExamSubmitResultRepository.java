package deepple.deepple.datingexam.application.required;

import deepple.deepple.datingexam.domain.DatingExamSubmitResult;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface DatingExamSubmitResultRepository extends Repository<DatingExamSubmitResult, Long> {
    Optional<DatingExamSubmitResult> findByMemberId(Long memberId);

    DatingExamSubmitResult save(DatingExamSubmitResult result);
}
