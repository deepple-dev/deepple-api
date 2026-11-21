package kr.co.deepple.feed.application.port.out;

import java.time.LocalDateTime;
import java.util.Set;
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
      SELECT CASE WHEN b.blockerId = :memberId THEN b.blockedId ELSE b.blockerId END
      FROM FeedBlock b
      WHERE b.blockerId = :memberId OR b.blockedId = :memberId
      """)
  Set<Long> findBlockRelatedMemberIds(long memberId);
}
