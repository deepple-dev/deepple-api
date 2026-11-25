package kr.co.deepple.image.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseEntity {

  private static final String SOURCE_PREFIX = "tmp";
  private static final String OUTPUT_PREFIX = "images";
  private static final int MAX_FAILURE_REASON_LENGTH = 255;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private ImageOwnerType ownerType;

  private long ownerId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private ImageContentType contentType;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private ImageStatus status;

  @Column(nullable = false)
  private String sourceKey;

  @Column(nullable = false)
  private String imageKey;

  @Column(nullable = false)
  private String thumbnailKey;

  private String failureReason;

  private Image(
      @NonNull ImageOwnerType ownerType, long ownerId, @NonNull ImageContentType contentType) {
    String path = ownerType.getPathSegment() + "/" + ownerId + "/" + UUID.randomUUID();
    this.ownerType = ownerType;
    this.ownerId = ownerId;
    this.contentType = contentType;
    this.status = ImageStatus.PROCESSING;
    this.sourceKey = SOURCE_PREFIX + "/" + path;
    this.imageKey = OUTPUT_PREFIX + "/" + path + ".webp";
    this.thumbnailKey = OUTPUT_PREFIX + "/" + path + "-thumbnail.webp";
  }

  public static Image uploadedByMember(long memberId, ImageContentType contentType) {
    return new Image(ImageOwnerType.MEMBER, memberId, contentType);
  }

  public static Image uploadedByAdmin(long adminId, ImageContentType contentType) {
    return new Image(ImageOwnerType.ADMIN, adminId, contentType);
  }

  public boolean isOwnedBy(ImageOwnerType ownerType, long ownerId) {
    return this.ownerType == ownerType && this.ownerId == ownerId;
  }

  public boolean isOwnedBy(ImageOwnerType ownerType) {
    return this.ownerType == ownerType;
  }

  public boolean isFailed() {
    return status == ImageStatus.FAILED;
  }

  public void complete() {
    if (status != ImageStatus.PROCESSING) {
      return;
    }
    this.status = ImageStatus.COMPLETED;
  }

  public void fail(String failureReason) {
    if (status != ImageStatus.PROCESSING) {
      return;
    }
    this.status = ImageStatus.FAILED;
    this.failureReason =
        failureReason == null || failureReason.length() <= MAX_FAILURE_REASON_LENGTH
            ? failureReason
            : failureReason.substring(0, MAX_FAILURE_REASON_LENGTH);
  }
}
