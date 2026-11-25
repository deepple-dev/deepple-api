package kr.co.deepple.feed.application.port.out;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedAuthorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FeedAuthorRepository extends JpaRepository<FeedAuthor, Long> {

  Optional<FeedAuthor> findByMemberId(long memberId);

  Optional<FeedAuthor> findFirstByAuthorType(FeedAuthorType authorType);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT a FROM FeedAuthor a WHERE a.memberId = :memberId")
  Optional<FeedAuthor> findByMemberIdForUpdate(long memberId);

  @Modifying
  @Query(
      value =
          """
          insert into feed_authors (author_type, member_id, created_at)
          values ('MEMBER', :memberId, :createdAt)
          on duplicate key update id = id
          """,
      nativeQuery = true)
  void insertIfAbsent(long memberId, LocalDateTime createdAt);
}
