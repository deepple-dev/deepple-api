package kr.co.deepple.feed.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;
import java.util.UUID;
import kr.co.deepple.feed.domain.PollFeed;
import kr.co.deepple.feed.domain.TextPosition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PollFeedFixture {

  private Long id;
  private long authorId = 1L;
  private String grade = "SILVER";
  private String question = "주말에 뭐 할까요?";
  private TextPosition textPosition = new TextPosition(0.5, 0.5, 0.0, 1.0);
  private Long backgroundId = 1L;
  private List<String> options = List.of("예", "아니오");

  private String idempotencyKey = UUID.randomUUID().toString();

  public static PollFeedFixture builder() {
    return new PollFeedFixture();
  }

  public PollFeed build() {
    PollFeed feed =
        PollFeed.create(
            authorId, grade, question, textPosition, backgroundId, null, options, idempotencyKey);
    setField(feed, "id", id);
    return feed;
  }
}
