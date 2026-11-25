package kr.co.deepple.feed.fixture;

import kr.co.deepple.feed.domain.FeedCount;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedCountFixture {

  private long feedId = 1L;
  private long likeCount;
  private long commentCount;

  public static FeedCountFixture builder() {
    return new FeedCountFixture();
  }

  public FeedCount build() {
    return FeedCount.of(feedId, likeCount, commentCount);
  }
}
