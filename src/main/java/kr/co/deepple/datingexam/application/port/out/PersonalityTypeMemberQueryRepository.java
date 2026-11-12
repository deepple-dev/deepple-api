package kr.co.deepple.datingexam.application.port.out;

import java.util.List;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;

public interface PersonalityTypeMemberQueryRepository {
  List<Long> findMemberIdsByDominantType(long memberId, AnswerPersonalityType type);
}
