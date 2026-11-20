package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.FeedView;

public interface GetFeedUseCase {
  FeedView getFeed(long memberId, long feedId);
}
