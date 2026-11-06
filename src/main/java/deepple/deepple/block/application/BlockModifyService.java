package deepple.deepple.block.application;

import deepple.deepple.block.application.provided.BlockCommander;
import deepple.deepple.block.application.required.BlockRepository;
import deepple.deepple.block.domain.Block;
import deepple.deepple.block.domain.BlockErrorCode;
import deepple.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockModifyService implements BlockCommander {
    private final BlockRepository blockRepository;

    @Override
    @Transactional
    public void createBlock(Long blockerId, Long blockedId) {
        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw new DeeppleException(
                BlockErrorCode.BLOCK_ALREADY_EXISTS,
                "blockerId=" + blockerId + ", blockedId=" + blockedId
            );
        }
        Block block = Block.of(blockerId, blockedId);
        blockRepository.save(block);
    }
}
