package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.dto.FeedCreateRequest;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.port.in.CreateFeedUseCase;
import kr.co.deepple.feed.application.port.in.GetFeedUseCase;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateFeedService implements CreateFeedUseCase {

  private final FeedWriter feedWriter;
  private final FeedRepository feedRepository;
  private final FeedAuthorReader feedAuthorReader;
  private final GetFeedUseCase getFeedUseCase;

  @Override
  public FeedView createFeed(long memberId, String idempotencyKey, FeedCreateRequest request) {
    FeedAuthor author = feedAuthorReader.getMember(memberId);
    long feedId =
        feedRepository
            .findByAuthorIdAndIdempotencyKey(author.getId(), idempotencyKey)
            .map(Feed::getId)
            .orElseGet(() -> create(author, idempotencyKey, request));
    return getFeedUseCase.getFeed(memberId, feedId);
  }

  private long create(FeedAuthor author, String idempotencyKey, FeedCreateRequest request) {
    try {
      return feedWriter.create(author, idempotencyKey, request);
    } catch (DataIntegrityViolationException e) {
      return feedRepository
          .findByAuthorIdAndIdempotencyKey(author.getId(), idempotencyKey)
          .map(Feed::getId)
          .orElseThrow(() -> e);
    }
  }
}
