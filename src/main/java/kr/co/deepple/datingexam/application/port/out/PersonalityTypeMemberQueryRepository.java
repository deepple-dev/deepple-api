package kr.co.deepple.datingexam.application.port.out;

import kr.co.deepple.datingexam.domain.AnswerPersonalityType;

import java.util.List;

public interface PersonalityTypeMemberQueryRepository {
    List<Long> findMemberIdsByDominantType(Long memberId, AnswerPersonalityType type);
}
