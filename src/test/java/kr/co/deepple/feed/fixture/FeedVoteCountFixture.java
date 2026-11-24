package kr.co.deepple.feed.fixture;

import kr.co.deepple.feed.domain.FeedVoteCount;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedVoteCountFixture {

  private long optionId = 1L;
  private long feedId = 1L;
  private long voteCount;

  public static FeedVoteCountFixture builder() {
    return new FeedVoteCountFixture();
  }

  public FeedVoteCount build() {
    return FeedVoteCount.of(optionId, feedId, voteCount);
  }
}
