package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.FeedGridViews;

public interface FindMyFeedsUseCase {
  FeedGridViews findMyFeeds(long memberId, String cursor);
}
