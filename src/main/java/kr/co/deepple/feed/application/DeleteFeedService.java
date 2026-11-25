package kr.co.deepple.feed.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.port.in.DeleteFeedUseCase;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedDeletedEvent;
import kr.co.deepple.feed.domain.FeedErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteFeedService implements DeleteFeedUseCase {

  private final FeedRepository feedRepository;
  private final FeedAuthorReader feedAuthorReader;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public void deleteFeed(long memberId, long feedId) {
    FeedAuthor author = feedAuthorReader.getMember(memberId);
    Feed feed =
        feedRepository
            .findByIdAndAuthorIdForUpdate(feedId, author.getId())
            .orElseThrow(() ->
                new DeeppleException(
                    FeedErrorCode.FEED_NOT_FOUND, "feedId=" + feedId + ", memberId=" + memberId));
    feed.delete();
    eventPublisher.publishEvent(new FeedDeletedEvent(feedId));
  }
}
