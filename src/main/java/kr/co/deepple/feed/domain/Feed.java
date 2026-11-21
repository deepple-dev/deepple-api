package kr.co.deepple.feed.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import kr.co.deepple.common.entity.SoftDeleteBaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "feeds")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name = "type",
    discriminatorType = DiscriminatorType.STRING,
    columnDefinition = "varchar(50)")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Feed extends SoftDeleteBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long authorId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private FeedStatus status;

  @Column(nullable = false)
  private String content;

  private LocalDateTime publishedAt;

  @Column(updatable = false)
  private String idempotencyKey;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "feed_id", nullable = false, updatable = false)
  @OrderBy("displayOrder")
  @BatchSize(size = 100)
  private List<FeedImage> images = new ArrayList<>();

  protected Feed(
      long authorId,
      @NonNull FeedStatus status,
      @NonNull String content,
      @NonNull List<FeedImage> images,
      String idempotencyKey) {
    this.authorId = authorId;
    this.status = status;
    this.content = content;
    this.images.addAll(images);
    this.idempotencyKey = idempotencyKey;
    if (status == FeedStatus.PUBLISHED) {
      this.publishedAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
  }

  public abstract FeedType getType();

  public abstract Set<String> getVisibleGrades();

  public abstract boolean isPinned();

  public boolean isWrittenBy(long authorId) {
    return this.authorId == authorId;
  }

  public boolean isPublished() {
    return status == FeedStatus.PUBLISHED;
  }

  public boolean isVisibleTo(String viewerGrade) {
    if (viewerGrade == null) {
      return false;
    }
    return isPublished() && getVisibleGrades().contains(viewerGrade);
  }

  public boolean isHiddenByReport() {
    return status == FeedStatus.HIDDEN_BY_REPORT;
  }
}
