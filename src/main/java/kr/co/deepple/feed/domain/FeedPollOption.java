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
@Table(name = "feed_poll_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedPollOption extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String content;

  private int displayOrder;

  private FeedPollOption(@NonNull String content, int displayOrder) {
    this.content = content;
    this.displayOrder = displayOrder;
  }

  public static FeedPollOption of(String content, int displayOrder) {
    return new FeedPollOption(content, displayOrder);
  }
}
