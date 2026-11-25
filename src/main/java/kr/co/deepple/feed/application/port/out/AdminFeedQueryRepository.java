package kr.co.deepple.feed.application.port.out;

import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.domain.OfficialFeed;
import kr.co.deepple.feed.domain.UserFeed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminFeedQueryRepository {

  Page<UserFeed> findUserFeeds(FeedStatus status, Pageable pageable);

  Page<OfficialFeed> findOfficialFeeds(FeedStatus status, Pageable pageable);
}
