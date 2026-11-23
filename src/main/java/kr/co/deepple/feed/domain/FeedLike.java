package kr.co.deepple.feed.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedLike extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long feedId;

  private long memberId;

  private FeedLike(long feedId, long memberId) {
    this.feedId = feedId;
    this.memberId = memberId;
  }

  public static FeedLike of(long feedId, long memberId) {
    return new FeedLike(feedId, memberId);
  }
}
