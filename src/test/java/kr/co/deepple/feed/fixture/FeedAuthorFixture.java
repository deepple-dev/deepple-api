package kr.co.deepple.feed.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.Instant;
import kr.co.deepple.feed.domain.FeedAuthor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedAuthorFixture {

  private Long id;
  private long memberId = 1L;
  private String activityStatus;
  private Instant activityStatusUpdatedAt;

  public static FeedAuthorFixture builder() {
    return new FeedAuthorFixture();
  }

  public FeedAuthor build() {
    FeedAuthor author = FeedAuthor.member(memberId);
    setField(author, "id", id);
    setField(author, "activityStatus", activityStatus);
    setField(author, "activityStatusUpdatedAt", activityStatusUpdatedAt);
    return author;
  }
}
