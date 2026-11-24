package kr.co.deepple.feed.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.feed.domain.FeedVote;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedVoteFixture {

  private Long id;
  private long feedId = 1L;
  private long memberId = 1L;
  private long optionId = 1L;

  public static FeedVoteFixture builder() {
    return new FeedVoteFixture();
  }

  public FeedVote build() {
    FeedVote vote = FeedVote.of(feedId, memberId, optionId);
    setField(vote, "id", id);
    return vote;
  }
}
