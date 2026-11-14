package kr.co.deepple.like.application.port.out;

import kr.co.deepple.like.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeCommandRepository extends JpaRepository<Like, Long> {
  boolean existsBySenderIdAndReceiverId(long senderId, long receiverId);
}
