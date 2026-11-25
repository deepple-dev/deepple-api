package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.dto.AdminOfficialFeedView;
import kr.co.deepple.feed.application.dto.AdminUserFeedView;
import kr.co.deepple.feed.application.port.in.ChangeAdminFeedStatusUseCase;
import kr.co.deepple.feed.domain.FeedStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeAdminFeedStatusService implements ChangeAdminFeedStatusUseCase {

  private final FeedWriter feedWriter;
  private final AdminFeedViewAssembler adminFeedViewAssembler;

  @Override
  @Transactional
  public AdminUserFeedView changeUserFeedStatus(long feedId, FeedStatus status) {
    return adminFeedViewAssembler.toUserFeedView(feedWriter.changeUserFeedStatus(feedId, status));
  }

  @Override
  @Transactional
  public AdminOfficialFeedView changeOfficialFeedStatus(long feedId, FeedStatus status) {
    return adminFeedViewAssembler.toOfficialFeedView(
        feedWriter.changeOfficialFeedStatus(feedId, status));
  }
}
