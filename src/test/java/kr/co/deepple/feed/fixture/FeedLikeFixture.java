package kr.co.deepple.feed.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.feed.domain.FeedLike;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedLikeFixture {

  private Long id;
  private long feedId = 1L;
  private long memberId = 1L;

  public static FeedLikeFixture builder() {
    return new FeedLikeFixture();
  }

  public FeedLike build() {
    FeedLike like = FeedLike.of(feedId, memberId);
    setField(like, "id", id);
    return like;
  }
}
