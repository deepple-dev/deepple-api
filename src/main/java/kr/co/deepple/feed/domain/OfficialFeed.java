package kr.co.deepple.feed.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.BatchSize;

@Entity
@DiscriminatorValue("OFFICIAL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OfficialFeed extends Feed {

  private static final int MAX_TITLE_LENGTH = 50;
  private static final int MAX_CONTENT_LENGTH = 200;
  private static final int MAX_IMAGE_COUNT = 5;

  private String title;

  private boolean isPinned;

  private Long adminId;

  @ElementCollection
  @CollectionTable(name = "feed_target_grades", joinColumns = @JoinColumn(name = "feed_id"))
  @Column(name = "grade", columnDefinition = "varchar(50)")
  @BatchSize(size = 100)
  private Set<String> targetGrades = new HashSet<>();

  private OfficialFeed(
      long authorId,
      long adminId,
      String title,
      String content,
      List<FeedImage> images,
      Set<String> targetGrades,
      boolean isPublished,
      boolean isPinned) {
    super(authorId, isPublished ? FeedStatus.PUBLISHED : FeedStatus.HIDDEN, content, images, null);
    this.adminId = adminId;
    this.title = title;
    this.targetGrades.addAll(targetGrades);
    this.isPinned = isPinned;
  }

  public static OfficialFeed create(
      long authorId,
      long adminId,
      @NonNull String title,
      @NonNull String content,
      @NonNull List<FeedImage> images,
      @NonNull Set<String> targetGrades,
      boolean isPublished,
      boolean isPinned) {
    validate(title, content, images);
    return new OfficialFeed(
        authorId, adminId, title, content, images, targetGrades, isPublished, isPinned);
  }

  private static void validate(String title, String content, List<FeedImage> images) {
    if (title.isBlank() || title.length() > MAX_TITLE_LENGTH) {
      throw new DeeppleException(
          FeedErrorCode.INVALID_FEED_CONTENT, "titleLength=" + title.length());
    }
    if (content.isBlank() || content.length() > MAX_CONTENT_LENGTH) {
      throw new DeeppleException(
          FeedErrorCode.INVALID_FEED_CONTENT, "contentLength=" + content.length());
    }
    if (images.isEmpty() || images.size() > MAX_IMAGE_COUNT) {
      throw new DeeppleException(FeedErrorCode.INVALID_FEED_IMAGES, "count=" + images.size());
    }
  }

  @Override
  public FeedType getType() {
    return FeedType.OFFICIAL;
  }

  @Override
  protected boolean isTargetGrade(String grade) {
    return targetGrades.isEmpty() || targetGrades.contains(grade);
  }
}
