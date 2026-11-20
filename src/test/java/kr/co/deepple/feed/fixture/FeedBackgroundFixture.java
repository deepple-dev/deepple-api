package kr.co.deepple.feed.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.feed.domain.FeedBackground;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedBackgroundFixture {

  private Long id;
  private String imageKey = "feed-backgrounds/1/v1/image.webp";
  private String thumbnailKey = "feed-backgrounds/1/v1/thumbnail.webp";
  private int displayOrder = 1;
  private boolean isHidden;

  public static FeedBackgroundFixture builder() {
    return new FeedBackgroundFixture();
  }

  public FeedBackground build() {
    FeedBackground background = FeedBackground.of(imageKey, thumbnailKey, displayOrder);
    setField(background, "id", id);
    setField(background, "isHidden", isHidden);
    return background;
  }
}
