package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.FeedGridViews;
import kr.co.deepple.feed.application.dto.FeedViews;

public interface FindMyFeedsUseCase {
  FeedGridViews findMyFeeds(long memberId, String cursor);

  FeedViews findMyFeedViews(long memberId, String cursor);
}
