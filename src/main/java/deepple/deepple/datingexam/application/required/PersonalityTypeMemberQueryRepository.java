package deepple.deepple.datingexam.application.required;

import deepple.deepple.datingexam.domain.AnswerPersonalityType;

import java.util.List;

public interface PersonalityTypeMemberQueryRepository {
    List<Long> findMemberIdsByDominantType(Long memberId, AnswerPersonalityType type);
}
