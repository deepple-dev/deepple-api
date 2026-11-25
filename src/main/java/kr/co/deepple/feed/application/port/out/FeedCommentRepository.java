package kr.co.deepple.feed.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.feed.application.dto.FeedCommentCount;
import kr.co.deepple.feed.domain.FeedComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {

  Optional<FeedComment> findByIdAndAuthorId(long id, long authorId);

  long countByFeedId(long feedId);

  @Modifying
  @Query(
      """
      UPDATE FeedComment c SET c.deletedAt = :now
      WHERE c.feedId = :feedId AND c.deletedAt IS NULL
      """)
  int softDeleteByFeedId(long feedId, LocalDateTime now);

  @Query("SELECT coalesce(max(c.feedId), 0) FROM FeedComment c")
  long findMaxFeedId();

  @Query(
      """
      SELECT new kr.co.deepple.feed.application.dto.FeedCommentCount(c.feedId, count(c))
      FROM FeedComment c
      WHERE c.feedId BETWEEN :fromFeedId AND :toFeedId
      GROUP BY c.feedId
      """)
  List<FeedCommentCount> countByFeedIdBetween(long fromFeedId, long toFeedId);
}
