package kr.co.deepple.datingexam.application;

import kr.co.deepple.datingexam.application.port.in.PersonalityTypeMemberFinder;
import kr.co.deepple.datingexam.application.port.out.PersonalityTypeMemberQueryRepository;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class PersonalityTypeMemberQueryService implements PersonalityTypeMemberFinder {
    private final PersonalityTypeMemberQueryRepository personalityTypeMemberQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Long> findMemberIdsByDominantType(Long memberId, AnswerPersonalityType type) {
        return personalityTypeMemberQueryRepository.findMemberIdsByDominantType(memberId, type);
    }
}
