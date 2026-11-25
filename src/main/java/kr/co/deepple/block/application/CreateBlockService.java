package kr.co.deepple.block.application;

import java.time.LocalDateTime;
import kr.co.deepple.block.application.port.in.CreateBlockUseCase;
import kr.co.deepple.block.application.port.out.BlockRepository;
import kr.co.deepple.block.domain.Block;
import kr.co.deepple.block.event.BlockCreatedEvent;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateBlockService implements CreateBlockUseCase {

  private final BlockRepository blockRepository;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public void createBlock(long blockerId, long blockedId) {
    if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
      return;
    }
    Block block = Block.of(blockerId, blockedId);
    validateMemberExists(blockedId);
    blockRepository.insertIfAbsent(block.getBlockerId(), block.getBlockedId(), LocalDateTime.now());
    eventPublisher.publishEvent(BlockCreatedEvent.of(blockerId, blockedId));
  }

  private void validateMemberExists(long memberId) {
    findMemberSummaryUseCase.getMemberSummary(memberId);
  }
}
