package kr.co.deepple.feed.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.LocalDateTime;
import kr.co.deepple.feed.domain.FeedComment;
import kr.co.deepple.feed.domain.FeedCommentStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedCommentFixture {

  private Long id;
  private long feedId = 1L;
  private Long parentId;
  private long authorId = 1L;
  private String content = "저도 가고 싶어요";
  private FeedCommentStatus status;
  private LocalDateTime deletedAt;

  public static FeedCommentFixture builder() {
    return new FeedCommentFixture();
  }

  public FeedComment build() {
    FeedComment comment = FeedComment.create(feedId, authorId, content);
    setField(comment, "id", id);
    setField(comment, "parentId", parentId);
    if (status != null) {
      setField(comment, "status", status);
    }
    setField(comment, "deletedAt", deletedAt);
    return comment;
  }
}
