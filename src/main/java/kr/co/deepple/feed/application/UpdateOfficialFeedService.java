package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.dto.AdminFeedUpdateRequest;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedView;
import kr.co.deepple.feed.application.port.in.UpdateOfficialFeedUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateOfficialFeedService implements UpdateOfficialFeedUseCase {

  private final FeedWriter feedWriter;
  private final AdminFeedViewAssembler adminFeedViewAssembler;

  @Override
  @Transactional
  public AdminOfficialFeedView updateOfficialFeed(long feedId, AdminFeedUpdateRequest request) {
    return adminFeedViewAssembler.toOfficialFeedView(feedWriter.updateOfficial(feedId, request));
  }
}
