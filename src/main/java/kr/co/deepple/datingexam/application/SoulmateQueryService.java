package kr.co.deepple.datingexam.application;

import kr.co.deepple.datingexam.application.provided.SoulmateFinder;
import kr.co.deepple.datingexam.application.required.SoulmateQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Set;


@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class SoulmateQueryService implements SoulmateFinder {
    private final SoulmateQueryRepository soulmateQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findSoulmateIds(Long memberId) {
        return soulmateQueryRepository.findSameAnswerMemberIds(memberId);
    }
}
