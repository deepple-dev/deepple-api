package kr.co.deepple.feed.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.feed.domain.FeedCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface FeedCountRepository extends JpaRepository<FeedCount, Long> {

  @Transactional
  @Modifying
  @Query(
      value =
          """
          insert into feed_counts (feed_id, like_count, created_at)
          values (:feedId, :likeCount, :now) as new
          on duplicate key update like_count = new.like_count, updated_at = :now
          """,
      nativeQuery = true)
  void upsertLikeCount(long feedId, long likeCount, LocalDateTime now);

  @Transactional
  @Modifying
  @Query(
      value =
          """
          insert into feed_counts (feed_id, like_count, comment_count, created_at)
          values (:feedId, 0, :commentCount, :now) as new
          on duplicate key update comment_count = new.comment_count, updated_at = :now
          """,
      nativeQuery = true)
  void upsertCommentCount(long feedId, long commentCount, LocalDateTime now);

  @Query("SELECT coalesce(max(c.feedId), 0) FROM FeedCount c")
  long findMaxFeedId();

  List<FeedCount> findByFeedIdBetween(long fromFeedId, long toFeedId);
}
