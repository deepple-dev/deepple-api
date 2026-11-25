package kr.co.deepple.datingexam.application.port.in;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface FindDominantPersonalityTypesUseCase {
  Map<Long, String> findDominantPersonalityTypes(Collection<Long> memberIds);

  Optional<DominantPersonalityType> findDominantPersonalityType(long memberId);
}
