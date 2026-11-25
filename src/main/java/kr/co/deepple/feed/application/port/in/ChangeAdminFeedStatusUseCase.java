package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.AdminOfficialFeedView;
import kr.co.deepple.feed.application.dto.AdminUserFeedView;
import kr.co.deepple.feed.domain.FeedStatus;

public interface ChangeAdminFeedStatusUseCase {

  AdminUserFeedView changeUserFeedStatus(long feedId, FeedStatus status);

  AdminOfficialFeedView changeOfficialFeedStatus(long feedId, FeedStatus status);
}
