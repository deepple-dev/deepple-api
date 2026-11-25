package kr.co.deepple.feed.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.domain.OfficialFeed;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OfficialFeedFixture {

  private Long id;
  private long authorId = 1L;
  private long adminId = 1L;
  private String title = "공지";
  private String content = "11월 이벤트 안내";
  private List<FeedImage> images =
      List.of(FeedImage.of(1L, "images/admins/1/1.webp", "images/admins/1/1-thumbnail.webp", 0));
  private Set<String> targetGrades = Set.of();
  private boolean isPublished = true;
  private boolean isPinned;
  private String idempotencyKey;

  private LocalDateTime publishedAt;

  public static OfficialFeedFixture builder() {
    return new OfficialFeedFixture();
  }

  public OfficialFeed build() {
    OfficialFeed feed =
        OfficialFeed.create(
            authorId,
            adminId,
            title,
            content,
            images,
            targetGrades,
            isPublished,
            isPinned,
            idempotencyKey);
    setField(feed, "id", id);
    if (publishedAt != null) {
      setField(feed, "publishedAt", publishedAt);
    }
    return feed;
  }
}
