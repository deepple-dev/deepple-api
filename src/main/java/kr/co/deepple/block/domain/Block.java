package kr.co.deepple.block.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Block extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private long blockerId;

  @Column(nullable = false)
  private long blockedId;

  private Block(long blockerId, long blockedId) {
    if (blockerId == blockedId) {
      throw new DeeppleException(BlockErrorCode.SELF_BLOCK, "memberId=" + blockerId);
    }
    this.blockerId = blockerId;
    this.blockedId = blockedId;
  }

  public static Block of(long blockerId, long blockedId) {
    return new Block(blockerId, blockedId);
  }
}
