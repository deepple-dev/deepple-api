package kr.co.deepple.feed.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import java.util.List;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UserFeed extends Feed {

  @Column(columnDefinition = "varchar(50)")
  private String grade;

  private Long backgroundId;

  @Embedded private TextPosition textPosition;

  protected UserFeed(
      long authorId,
      @NonNull String grade,
      @NonNull String content,
      @NonNull TextPosition textPosition,
      Long backgroundId,
      FeedImage image,
      @NonNull String idempotencyKey) {
    super(
        authorId,
        image == null ? FeedStatus.PUBLISHED : FeedStatus.PENDING,
        content,
        image == null ? List.of() : List.of(image),
        idempotencyKey);
    this.grade = grade;
    this.textPosition = textPosition;
    this.backgroundId = backgroundId;
  }

  protected static void validateCanvas(Long backgroundId, FeedImage image) {
    boolean hasBackground = backgroundId != null;
    boolean hasImage = image != null;
    if (hasBackground && hasImage) {
      throw invalidCanvasException(backgroundId, hasImage);
    }
    if (!hasBackground && !hasImage) {
      throw invalidCanvasException(backgroundId, hasImage);
    }
  }

  private static DeeppleException invalidCanvasException(Long backgroundId, boolean hasImage) {
    return new DeeppleException(
        FeedErrorCode.INVALID_FEED_CANVAS,
        "backgroundId=" + backgroundId + ", hasImage=" + hasImage);
  }

  @Override
  protected boolean isTargetGrade(String grade) {
    return this.grade.equals(grade);
  }
}
