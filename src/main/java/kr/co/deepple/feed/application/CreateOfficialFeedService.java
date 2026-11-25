package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.dto.AdminFeedCreateRequest;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedView;
import kr.co.deepple.feed.application.port.in.CreateOfficialFeedUseCase;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateOfficialFeedService implements CreateOfficialFeedUseCase {

  private final FeedWriter feedWriter;
  private final FeedRepository feedRepository;
  private final FeedAuthorReader feedAuthorReader;
  private final AdminFeedViewAssembler adminFeedViewAssembler;

  @Override
  public AdminOfficialFeedView createOfficialFeed(
      long adminId, String idempotencyKey, AdminFeedCreateRequest request) {
    FeedAuthor official = feedAuthorReader.getOfficial();
    long feedId =
        feedRepository
            .findByAuthorIdAndIdempotencyKey(official.getId(), idempotencyKey)
            .map(Feed::getId)
            .orElseGet(() -> create(official, adminId, idempotencyKey, request));
    return adminFeedViewAssembler.getOfficialFeedView(feedId);
  }

  private long create(
      FeedAuthor official, long adminId, String idempotencyKey, AdminFeedCreateRequest request) {
    try {
      return feedWriter.createOfficial(official, adminId, idempotencyKey, request);
    } catch (DataIntegrityViolationException e) {
      return feedRepository
          .findByAuthorIdAndIdempotencyKey(official.getId(), idempotencyKey)
          .map(Feed::getId)
          .orElseThrow(() -> e);
    }
  }
}
