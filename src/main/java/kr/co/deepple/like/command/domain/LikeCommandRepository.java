package kr.co.deepple.like.command.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeCommandRepository extends JpaRepository<Like, Long> {
  boolean existsBySenderIdAndReceiverId(long senderId, long receiverId);
}
