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
@Table(name = "feed_backgrounds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedBackground extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String imageKey;

  @Column(nullable = false)
  private String thumbnailKey;

  private int displayOrder;

  private boolean isHidden;

  private FeedBackground(@NonNull String imageKey, @NonNull String thumbnailKey, int displayOrder) {
    this.imageKey = imageKey;
    this.thumbnailKey = thumbnailKey;
    this.displayOrder = displayOrder;
  }

  public static FeedBackground of(String imageKey, String thumbnailKey, int displayOrder) {
    return new FeedBackground(imageKey, thumbnailKey, displayOrder);
  }
}
