package kr.co.deepple.block.domain;

import kr.co.deepple.common.entity.BaseEntity;
import jakarta.persistence.*;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(
    name = "blocks",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_blocker_id_blocked_id", columnNames = {"blockerId", "blockedId"})
    },
    indexes = {
        @Index(name = "idx_block_blocked_id", columnList = "blockedId")
    }
)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class Block extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long blockerId;

    @Column(nullable = false)
    private Long blockedId;

    private Block(@NonNull Long blockerId, @NonNull Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new DeeppleException(BlockErrorCode.SELF_BLOCK, "memberId=" + blockerId);
        }
        this.blockerId = blockerId;
        this.blockedId = blockedId;
    }

    public static Block of(Long blockerId, Long blockedId) {
        return new Block(blockerId, blockedId);
    }
}
