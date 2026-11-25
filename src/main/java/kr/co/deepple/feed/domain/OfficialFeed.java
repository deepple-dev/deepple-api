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
  private static final Set<FeedStatus> CHANGEABLE_STATUSES =
      Set.of(FeedStatus.PUBLISHED, FeedStatus.HIDDEN);

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
      boolean isPinned,
      String idempotencyKey) {
    super(
        authorId,
        isPublished ? FeedStatus.PUBLISHED : FeedStatus.HIDDEN,
        content,
        images,
        idempotencyKey);
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
      boolean isPinned,
      String idempotencyKey) {
    validateTitle(title);
    validateContent(content);
    validateImages(images);
    validateTargetGrades(targetGrades);
    return new OfficialFeed(
        authorId,
        adminId,
        title,
        content,
        images,
        targetGrades,
        isPublished,
        isPinned,
        idempotencyKey);
  }

  public void changeTitle(@NonNull String title) {
    validateTitle(title);
    this.title = title;
  }

  public void changeContent(@NonNull String content) {
    validateContent(content);
    applyContent(content);
  }

  public void changeImages(@NonNull List<FeedImage> images) {
    validateImages(images);
    applyImages(images);
  }

  public void changeTargetGrades(@NonNull Set<String> targetGrades) {
    validateTargetGrades(targetGrades);
    this.targetGrades.clear();
    this.targetGrades.addAll(targetGrades);
  }

  public void changePinned(boolean isPinned) {
    this.isPinned = isPinned;
  }

  @Override
  protected Set<FeedStatus> changeableStatuses() {
    return CHANGEABLE_STATUSES;
  }

  private static void validateTitle(String title) {
    if (title.isBlank() || title.length() > MAX_TITLE_LENGTH) {
      throw new DeeppleException(
          FeedErrorCode.INVALID_FEED_CONTENT, "titleLength=" + title.length());
    }
  }

  private static void validateContent(String content) {
    if (content.isBlank() || content.length() > MAX_CONTENT_LENGTH) {
      throw new DeeppleException(
          FeedErrorCode.INVALID_FEED_CONTENT, "contentLength=" + content.length());
    }
  }

  private static void validateImages(List<FeedImage> images) {
    if (images.isEmpty() || images.size() > MAX_IMAGE_COUNT) {
      throw new DeeppleException(FeedErrorCode.INVALID_FEED_IMAGES, "count=" + images.size());
    }
  }

  private static void validateTargetGrades(Set<String> targetGrades) {
    if (!FeedGrades.ALL.containsAll(targetGrades)) {
      throw new DeeppleException(
          FeedErrorCode.INVALID_FEED_TARGET_GRADES, "targetGrades=" + targetGrades);
    }
  }

  @Override
  public FeedType getType() {
    return FeedType.OFFICIAL;
  }

  @Override
  public boolean isPinned() {
    return isPinned;
  }

  @Override
  public Set<String> getVisibleGrades() {
    if (targetGrades.isEmpty()) {
      return FeedGrades.ALL;
    }
    return Set.copyOf(targetGrades);
  }
}
