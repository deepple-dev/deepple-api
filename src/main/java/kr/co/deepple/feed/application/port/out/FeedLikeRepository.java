package kr.co.deepple.feed.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.feed.application.dto.FeedLikeCount;
import kr.co.deepple.feed.domain.FeedLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

  @Transactional
  @Modifying
  @Query(
      value =
          """
          insert ignore into feed_likes (feed_id, member_id, created_at)
          values (:feedId, :memberId, :createdAt)
          """,
      nativeQuery = true)
  int insertIfAbsent(long feedId, long memberId, LocalDateTime createdAt);

  @Transactional
  @Modifying
  @Query("DELETE FROM FeedLike l WHERE l.feedId = :feedId AND l.memberId = :memberId")
  int deleteByFeedIdAndMemberId(long feedId, long memberId);

  long countByFeedId(long feedId);

  @Query("SELECT l.feedId FROM FeedLike l WHERE l.memberId = :memberId")
  List<Long> findFeedIdsByMemberId(long memberId);

  @Query("SELECT coalesce(max(l.feedId), 0) FROM FeedLike l")
  long findMaxFeedId();

  @Query(
      """
      SELECT new kr.co.deepple.feed.application.dto.FeedLikeCount(l.feedId, count(l))
      FROM FeedLike l
      WHERE l.feedId BETWEEN :fromFeedId AND :toFeedId
      GROUP BY l.feedId
      """)
  List<FeedLikeCount> countByFeedIdBetween(long fromFeedId, long toFeedId);
}
