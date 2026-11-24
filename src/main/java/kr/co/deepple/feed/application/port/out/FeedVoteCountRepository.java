package kr.co.deepple.feed.application.port.out;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import kr.co.deepple.feed.domain.FeedVoteCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface FeedVoteCountRepository extends JpaRepository<FeedVoteCount, Long> {

  @Transactional
  @Modifying
  @Query(
      value =
          """
          insert into feed_vote_counts (option_id, feed_id, vote_count, created_at)
          values (:optionId, :feedId, :voteCount, :now) as new
          on duplicate key update vote_count = new.vote_count, updated_at = :now
          """,
      nativeQuery = true)
  void upsertVoteCount(long optionId, long feedId, long voteCount, LocalDateTime now);

  List<FeedVoteCount> findByFeedIdIn(Collection<Long> feedIds);

  @Query("SELECT coalesce(max(c.feedId), 0) FROM FeedVoteCount c")
  long findMaxFeedId();

  List<FeedVoteCount> findByFeedIdBetween(long fromFeedId, long toFeedId);
}
