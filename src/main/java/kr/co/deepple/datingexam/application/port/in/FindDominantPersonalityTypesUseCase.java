package kr.co.deepple.datingexam.application.port.in;

import java.util.Collection;
import java.util.Map;

public interface FindDominantPersonalityTypesUseCase {
  Map<Long, String> findDominantPersonalityTypes(Collection<Long> memberIds);
}
