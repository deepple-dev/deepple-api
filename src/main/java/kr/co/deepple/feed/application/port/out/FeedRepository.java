package kr.co.deepple.feed.application.port.out;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.feed.application.dto.HomeFeedSortKey;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.domain.OfficialFeed;
import kr.co.deepple.feed.domain.UserFeed;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface FeedRepository extends JpaRepository<Feed, Long> {

  Optional<Feed> findByIdAndDeletedAtIsNull(long id);

  @Query("SELECT f FROM OfficialFeed f WHERE f.id = :id AND f.deletedAt IS NULL")
  Optional<OfficialFeed> findOfficialFeedById(long id);

  @Query("SELECT f FROM UserFeed f WHERE f.id = :id")
  Optional<UserFeed> findUserFeedIncludingDeletedById(long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT f FROM UserFeed f WHERE f.id = :id AND f.deletedAt IS NULL")
  Optional<UserFeed> findUserFeedByIdForUpdate(long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT f FROM OfficialFeed f WHERE f.id = :id AND f.deletedAt IS NULL")
  Optional<OfficialFeed> findOfficialFeedByIdForUpdate(long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT f FROM Feed f WHERE f.id = :id AND f.authorId = :authorId AND f.deletedAt IS NULL")
  Optional<Feed> findByIdAndAuthorIdForUpdate(long id, long authorId);

  List<Feed> findByAuthorIdAndDeletedAtIsNullOrderByIdDesc(long authorId, Limit limit);

  List<Feed> findByAuthorIdAndIdLessThanAndDeletedAtIsNullOrderByIdDesc(
      long authorId, long id, Limit limit);

  long countByAuthorIdAndDeletedAtIsNull(long authorId);

  Optional<Feed> findByAuthorIdAndIdempotencyKey(long authorId, String idempotencyKey);

  List<Feed> findByIdInAndStatusAndDeletedAtIsNull(Collection<Long> ids, FeedStatus status);

  @Query(
      """
      SELECT new kr.co.deepple.feed.application.dto.HomeFeedSortKey(f.id, false, f.publishedAt)
      FROM UserFeed f JOIN FeedAuthor a ON a.id = f.authorId
      WHERE f.grade = :grade
        AND f.status = kr.co.deepple.feed.domain.FeedStatus.PUBLISHED
        AND f.deletedAt IS NULL
        AND a.activityStatus = 'ACTIVE'
      """)
  List<HomeFeedSortKey> findHomeUserFeedSortKeys(String grade);

  @Query(
      """
      SELECT new kr.co.deepple.feed.application.dto.HomeFeedSortKey(f.id, f.isPinned, f.publishedAt)
      FROM OfficialFeed f
      WHERE f.status = kr.co.deepple.feed.domain.FeedStatus.PUBLISHED
        AND f.deletedAt IS NULL
        AND (f.targetGrades IS EMPTY OR :grade MEMBER OF f.targetGrades)
      """)
  List<HomeFeedSortKey> findHomeOfficialFeedSortKeys(String grade);

  @Query(
      """
      SELECT f FROM UserFeed f
      WHERE f.authorId = :authorId
        AND f.status = kr.co.deepple.feed.domain.FeedStatus.PUBLISHED
        AND f.deletedAt IS NULL
      """)
  List<UserFeed> findPublishedUserFeedsByAuthorId(long authorId);

  @Query(
      """
      SELECT f FROM OfficialFeed f
      WHERE f.status = kr.co.deepple.feed.domain.FeedStatus.PUBLISHED
        AND f.deletedAt IS NULL
        AND (f.targetGrades IS EMPTY OR :grade MEMBER OF f.targetGrades)
      ORDER BY f.isPinned DESC, f.publishedAt DESC, f.id DESC
      """)
  List<OfficialFeed> findFirstOfficialFeeds(String grade, Limit limit);

  @Query(
      """
      SELECT COUNT(f) FROM OfficialFeed f
      WHERE f.status = kr.co.deepple.feed.domain.FeedStatus.PUBLISHED
        AND f.deletedAt IS NULL
        AND (f.targetGrades IS EMPTY OR :grade MEMBER OF f.targetGrades)
      """)
  long countOfficialFeeds(String grade);

  @Query(
      """
      SELECT f FROM OfficialFeed f
      WHERE f.status = kr.co.deepple.feed.domain.FeedStatus.PUBLISHED
        AND f.deletedAt IS NULL
        AND (f.targetGrades IS EMPTY OR :grade MEMBER OF f.targetGrades)
        AND ((:isPinned = true AND f.isPinned = false)
          OR (f.isPinned = :isPinned
            AND (f.publishedAt < :publishedAt
              OR (f.publishedAt = :publishedAt AND f.id < :feedId))))
      ORDER BY f.isPinned DESC, f.publishedAt DESC, f.id DESC
      """)
  List<OfficialFeed> findOfficialFeedsAfter(
      String grade, boolean isPinned, LocalDateTime publishedAt, long feedId, Limit limit);
}
