package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.AdminOfficialFeedSearchCondition;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedView;
import kr.co.deepple.feed.application.dto.AdminUserFeedDetailView;
import kr.co.deepple.feed.application.dto.AdminUserFeedSearchCondition;
import kr.co.deepple.feed.application.dto.AdminUserFeedView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindAdminFeedsUseCase {

  AdminUserFeedDetailView getUserFeed(long feedId);

  Page<AdminUserFeedView> findUserFeeds(AdminUserFeedSearchCondition condition, Pageable pageable);

  Page<AdminOfficialFeedView> findOfficialFeeds(
      AdminOfficialFeedSearchCondition condition, Pageable pageable);
}
