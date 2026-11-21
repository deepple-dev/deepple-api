package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.FeedGridViews;

public interface FindOfficialFeedsUseCase {

  FeedGridViews findOfficialFeeds(long memberId, String cursor);
}
