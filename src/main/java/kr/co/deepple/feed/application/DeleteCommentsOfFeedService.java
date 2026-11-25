package kr.co.deepple.feed.application;

import java.time.LocalDateTime;
import kr.co.deepple.feed.application.port.in.DeleteCommentsOfFeedUseCase;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCommentsOfFeedService implements DeleteCommentsOfFeedUseCase {

  private final FeedCommentRepository feedCommentRepository;

  @Override
  @Transactional
  public void deleteCommentsOfFeed(long feedId) {
    feedCommentRepository.softDeleteByFeedId(feedId, LocalDateTime.now());
  }
}
