package kr.co.deepple.datingexam.application.port.out;

import kr.co.deepple.datingexam.domain.DatingExamSubmit;
import org.springframework.data.repository.Repository;

import java.util.Set;

public interface DatingExamSubmitRepository extends Repository<DatingExamSubmit, Long> {
    DatingExamSubmit save(DatingExamSubmit datingExamSubmit);

    boolean existsByMemberIdAndSubjectId(Long memberId, Long subjectId);

    Set<DatingExamSubmit> findAllByMemberId(Long memberId);
}
