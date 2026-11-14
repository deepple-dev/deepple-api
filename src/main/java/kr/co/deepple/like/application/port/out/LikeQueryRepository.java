package kr.co.deepple.like.application.port.out;

import java.util.List;
import java.util.Set;
import kr.co.deepple.like.application.dto.RawLikeView;

public interface LikeQueryRepository {
  List<RawLikeView> findSentLikes(long senderId, Long lastLikeId, Set<Long> blockedIds);

  List<RawLikeView> findReceivedLikes(long receiverId, Long lastLikeId, Set<Long> blockedIds);
}
