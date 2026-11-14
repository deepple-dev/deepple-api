package kr.co.deepple.like.application.port.in;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface FindLikeLevelUseCase {
  Optional<String> findLikeLevel(long senderId, long receiverId);

  Map<Long, String> findLikeLevels(long senderId, Collection<Long> receiverIds);
}
