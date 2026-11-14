package kr.co.deepple.datingexam.application.port.out;

import java.util.Set;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;

public interface PersonalityTypeMemberQueryRepository {
  Set<Long> findMemberIdsByDominantType(AnswerPersonalityType type);
}
