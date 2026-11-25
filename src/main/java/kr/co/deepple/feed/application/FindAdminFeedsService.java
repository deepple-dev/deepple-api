package kr.co.deepple.feed.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedSearchCondition;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedView;
import kr.co.deepple.feed.application.dto.AdminUserFeedDetailView;
import kr.co.deepple.feed.application.dto.AdminUserFeedSearchCondition;
import kr.co.deepple.feed.application.dto.AdminUserFeedView;
import kr.co.deepple.feed.application.port.in.FindAdminFeedsUseCase;
import kr.co.deepple.feed.application.port.out.AdminFeedQueryRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.domain.UserFeed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindAdminFeedsService implements FindAdminFeedsUseCase {

  private final FeedRepository feedRepository;
  private final AdminFeedQueryRepository adminFeedQueryRepository;
  private final AdminFeedViewAssembler adminFeedViewAssembler;

  @Override
  @Transactional(readOnly = true)
  public AdminUserFeedDetailView getUserFeed(long feedId) {
    return feedRepository
        .findUserFeedIncludingDeletedById(feedId)
        .map(adminFeedViewAssembler::toUserFeedDetailView)
        .orElseThrow(() -> new DeeppleException(FeedErrorCode.FEED_NOT_FOUND, "feedId=" + feedId));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AdminUserFeedView> findUserFeeds(
      AdminUserFeedSearchCondition condition, Pageable pageable) {
    Page<UserFeed> feeds =
        adminFeedQueryRepository.findUserFeeds(FeedStatus.from(condition.status()), pageable);
    return new PageImpl<>(
        adminFeedViewAssembler.toUserFeedViews(feeds.getContent()),
        pageable,
        feeds.getTotalElements());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AdminOfficialFeedView> findOfficialFeeds(
      AdminOfficialFeedSearchCondition condition, Pageable pageable) {
    return adminFeedQueryRepository
        .findOfficialFeeds(FeedStatus.from(condition.status()), pageable)
        .map(adminFeedViewAssembler::toOfficialFeedView);
  }
}
