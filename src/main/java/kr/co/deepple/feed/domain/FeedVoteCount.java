package kr.co.deepple.feed.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_vote_counts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedVoteCount extends BaseEntity {

  @Id private long optionId;

  private long feedId;

  private long voteCount;

  private FeedVoteCount(long optionId, long feedId, long voteCount) {
    this.optionId = optionId;
    this.feedId = feedId;
    this.voteCount = voteCount;
  }

  public static FeedVoteCount of(long optionId, long feedId, long voteCount) {
    return new FeedVoteCount(optionId, feedId, voteCount);
  }
}
