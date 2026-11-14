package kr.co.deepple.like.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.like.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeCommandRepository extends JpaRepository<Like, Long> {
  Optional<Like> findBySenderIdAndReceiverId(long senderId, long receiverId);

  List<Like> findAllBySenderIdAndReceiverIdIn(long senderId, Collection<Long> receiverIds);

  boolean existsBySenderIdAndReceiverId(long senderId, long receiverId);
}
