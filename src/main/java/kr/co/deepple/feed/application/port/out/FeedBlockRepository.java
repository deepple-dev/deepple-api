package kr.co.deepple.feed.application.port.out;

import java.time.LocalDateTime;
import kr.co.deepple.feed.domain.FeedBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FeedBlockRepository extends JpaRepository<FeedBlock, Long> {

  @Modifying
  @Query(
      value =
          """
          insert into feed_blocks (blocker_id, blocked_id, created_at)
          values (:blockerId, :blockedId, :createdAt)
          on duplicate key update id = id
          """,
      nativeQuery = true)
  void insertIfAbsent(long blockerId, long blockedId, LocalDateTime createdAt);

  @Query(
      """
      SELECT COUNT(b) > 0 FROM FeedBlock b
      WHERE (b.blockerId = :memberId AND b.blockedId = :otherMemberId)
         OR (b.blockerId = :otherMemberId AND b.blockedId = :memberId)
      """)
  boolean existsBetween(long memberId, long otherMemberId);
}
