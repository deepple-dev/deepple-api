package kr.co.deepple.feed.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.feed.application.dto.PollOptionVoteCount;
import kr.co.deepple.feed.application.dto.VotedOption;
import kr.co.deepple.feed.domain.FeedVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface FeedVoteRepository extends JpaRepository<FeedVote, Long> {

  @Transactional
  @Modifying
  @Query(
      value =
          """
          insert ignore into feed_votes (feed_id, member_id, option_id, created_at)
          values (:feedId, :memberId, :optionId, :createdAt)
          """,
      nativeQuery = true)
  int insertIfAbsent(long feedId, long memberId, long optionId, LocalDateTime createdAt);

  @Query(
      """
      SELECT new kr.co.deepple.feed.application.dto.PollOptionVoteCount(v.feedId, v.optionId, count(v))
      FROM FeedVote v
      WHERE v.feedId = :feedId
      GROUP BY v.feedId, v.optionId
      """)
  List<PollOptionVoteCount> countByFeedId(long feedId);

  @Query(
      """
      SELECT new kr.co.deepple.feed.application.dto.VotedOption(v.feedId, v.optionId)
      FROM FeedVote v
      WHERE v.memberId = :memberId
      """)
  List<VotedOption> findVotedOptionsByMemberId(long memberId);

  @Query("SELECT coalesce(max(v.feedId), 0) FROM FeedVote v")
  long findMaxFeedId();

  @Query(
      """
      SELECT new kr.co.deepple.feed.application.dto.PollOptionVoteCount(v.feedId, v.optionId, count(v))
      FROM FeedVote v
      WHERE v.feedId BETWEEN :fromFeedId AND :toFeedId
      GROUP BY v.feedId, v.optionId
      """)
  List<PollOptionVoteCount> countByFeedIdBetween(long fromFeedId, long toFeedId);
}
