package kr.co.deepple.recommendation.application.port.out;

import java.util.Optional;
import java.util.Set;
import kr.co.deepple.recommendation.application.dto.PersonalityIntroductionOpenState;

public interface PersonalityIntroductionOpenStateRepository {
  void saveOpenState(long memberId, PersonalityIntroductionOpenState state);

  Optional<PersonalityIntroductionOpenState> findOpenState(long memberId);

  Set<Long> findUnlockedMemberIds(long memberId);

  void addUnlockedMemberId(long memberId, long targetMemberId);
}
