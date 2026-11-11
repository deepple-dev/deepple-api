package kr.co.deepple.block.application;

import kr.co.deepple.block.application.provided.BlockCommander;
import kr.co.deepple.block.application.required.BlockRepository;
import kr.co.deepple.block.domain.Block;
import kr.co.deepple.block.domain.BlockErrorCode;
import kr.co.deepple.common.exception.DeeppleException;
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
        blockRepository.save(Block.of(blockerId, blockedId));
    }

    @Override
    @Transactional
    public void createBlockIfAbsent(long blockerId, long blockedId) {
        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            return;
        }
        blockRepository.save(Block.of(blockerId, blockedId));
    }
}
