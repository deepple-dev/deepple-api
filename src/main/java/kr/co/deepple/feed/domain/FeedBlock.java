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
@Table(name = "feed_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedBlock extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long blockerId;

  private long blockedId;

  private FeedBlock(long blockerId, long blockedId) {
    this.blockerId = blockerId;
    this.blockedId = blockedId;
  }

  public static FeedBlock of(long blockerId, long blockedId) {
    return new FeedBlock(blockerId, blockedId);
  }
}
