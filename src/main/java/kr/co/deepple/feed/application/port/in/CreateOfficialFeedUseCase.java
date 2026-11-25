package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.AdminFeedCreateRequest;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedView;

public interface CreateOfficialFeedUseCase {

  AdminOfficialFeedView createOfficialFeed(
      long adminId, String idempotencyKey, AdminFeedCreateRequest request);
}
