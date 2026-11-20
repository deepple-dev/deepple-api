package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.FeedCreateRequest;
import kr.co.deepple.feed.application.dto.FeedView;

public interface CreateFeedUseCase {
  FeedView createFeed(long memberId, String idempotencyKey, FeedCreateRequest request);
}
