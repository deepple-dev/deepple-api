package kr.co.deepple.like.application.port.in;

import java.util.Optional;

public interface FindLikeLevelUseCase {
  Optional<String> findLikeLevel(long senderId, long receiverId);
}
