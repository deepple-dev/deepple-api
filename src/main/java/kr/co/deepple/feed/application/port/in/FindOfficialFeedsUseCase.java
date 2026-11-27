package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.FeedViews;
import kr.co.deepple.feed.application.dto.OfficialFeedGridViews;

public interface FindOfficialFeedsUseCase {

  OfficialFeedGridViews findOfficialFeeds(long memberId, String cursor);

  FeedViews findOfficialFeedViews(long memberId, String cursor);
}
