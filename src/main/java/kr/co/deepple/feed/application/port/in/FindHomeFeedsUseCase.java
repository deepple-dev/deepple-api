package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.FeedViews;

public interface FindHomeFeedsUseCase {

  FeedViews findHomeFeeds(long memberId, String cursor);
}
