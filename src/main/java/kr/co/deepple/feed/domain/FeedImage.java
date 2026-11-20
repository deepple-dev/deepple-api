package kr.co.deepple.feed.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "feed_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedImage extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long imageId;

  @Column(nullable = false)
  private String imageKey;

  @Column(nullable = false)
  private String thumbnailKey;

  private int displayOrder;

  private FeedImage(
      long imageId, @NonNull String imageKey, @NonNull String thumbnailKey, int displayOrder) {
    this.imageId = imageId;
    this.imageKey = imageKey;
    this.thumbnailKey = thumbnailKey;
    this.displayOrder = displayOrder;
  }

  public static FeedImage of(long imageId, String imageKey, String thumbnailKey, int displayOrder) {
    return new FeedImage(imageId, imageKey, thumbnailKey, displayOrder);
  }
}
