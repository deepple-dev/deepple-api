package kr.co.deepple.feed.application;

import java.time.LocalDateTime;
import kr.co.deepple.feed.application.port.in.SyncFeedBlockUseCase;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncFeedBlockService implements SyncFeedBlockUseCase {

  private final FeedBlockRepository feedBlockRepository;

  @Override
  @Transactional
  public void syncBlock(long blockerId, long blockedId) {
    feedBlockRepository.insertIfAbsent(blockerId, blockedId, LocalDateTime.now());
  }
}
