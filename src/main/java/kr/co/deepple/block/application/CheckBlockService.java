package kr.co.deepple.block.application;

import java.util.Set;
import kr.co.deepple.block.application.port.in.CheckBlockUseCase;
import kr.co.deepple.block.application.port.in.FindBlockedMemberIdsUseCase;
import kr.co.deepple.block.application.port.out.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckBlockService implements CheckBlockUseCase, FindBlockedMemberIdsUseCase {

  private final BlockRepository blockRepository;

  @Override
  @Transactional(readOnly = true)
  public boolean isBlocked(long blockerId, long blockedId) {
    return blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Long> findBlockedMemberIds(long blockerId) {
    return blockRepository.findBlockedIdsByBlockerId(blockerId);
  }
}
