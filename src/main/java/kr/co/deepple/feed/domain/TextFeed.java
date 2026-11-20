package kr.co.deepple.feed.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@DiscriminatorValue("TEXT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TextFeed extends UserFeed {

  private static final int MAX_CONTENT_LENGTH = 50;

  private TextFeed(
      long authorId,
      String grade,
      String content,
      TextPosition textPosition,
      Long backgroundId,
      FeedImage image,
      String idempotencyKey) {
    super(authorId, grade, content, textPosition, backgroundId, image, idempotencyKey);
  }

  public static TextFeed create(
      long authorId,
      @NonNull String grade,
      @NonNull String content,
      @NonNull TextPosition textPosition,
      Long backgroundId,
      FeedImage image,
      @NonNull String idempotencyKey) {
    validateCanvas(backgroundId, image);
    validateContent(content);
    return new TextFeed(
        authorId, grade, content, textPosition, backgroundId, image, idempotencyKey);
  }

  private static void validateContent(String content) {
    if (content.isBlank() || content.length() > MAX_CONTENT_LENGTH) {
      throw new DeeppleException(FeedErrorCode.INVALID_FEED_CONTENT, "length=" + content.length());
    }
  }

  @Override
  public FeedType getType() {
    return FeedType.TEXT;
  }
}
