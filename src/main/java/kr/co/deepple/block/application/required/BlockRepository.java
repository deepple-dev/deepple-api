package kr.co.deepple.block.application.required;

import kr.co.deepple.block.domain.Block;
import org.springframework.data.repository.Repository;

public interface BlockRepository extends Repository<Block, Long> {
    Block save(Block block);

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}
