package kr.co.deepple.feed.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.UUID;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.domain.TextFeed;
import kr.co.deepple.feed.domain.TextPosition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextFeedFixture {

  private Long id;
  private long authorId = 1L;
  private String grade = "SILVER";
  private String content = "오늘 날씨가 좋아요";
  private TextPosition textPosition = new TextPosition(0.5, 0.5, 0.0, 1.0);
  private Long backgroundId = 1L;
  private FeedImage image;
  private FeedStatus status;

  private String idempotencyKey = UUID.randomUUID().toString();

  public static TextFeedFixture builder() {
    return new TextFeedFixture();
  }

  public TextFeed build() {
    TextFeed feed =
        TextFeed.create(
            authorId,
            grade,
            content,
            textPosition,
            image == null ? backgroundId : null,
            image,
            idempotencyKey);
    setField(feed, "id", id);
    if (status != null) {
      setField(feed, "status", status);
    }
    return feed;
  }
}
