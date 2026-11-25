package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.AdminFeedUpdateRequest;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedView;

public interface UpdateOfficialFeedUseCase {

  AdminOfficialFeedView updateOfficialFeed(long feedId, AdminFeedUpdateRequest request);
}
