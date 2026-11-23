package kr.co.deepple.feed.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_counts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedCount extends BaseEntity {

  @Id private long feedId;

  private long likeCount;

  private FeedCount(long feedId, long likeCount) {
    this.feedId = feedId;
    this.likeCount = likeCount;
  }

  public static FeedCount of(long feedId, long likeCount) {
    return new FeedCount(feedId, likeCount);
  }
}
