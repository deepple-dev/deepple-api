package kr.co.deepple.feed.application;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedView;
import kr.co.deepple.feed.application.dto.AdminUserFeedDetailView;
import kr.co.deepple.feed.application.dto.AdminUserFeedView;
import kr.co.deepple.feed.application.dto.FeedImageView;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.domain.FeedPollOption;
import kr.co.deepple.feed.domain.OfficialFeed;
import kr.co.deepple.feed.domain.PollFeed;
import kr.co.deepple.feed.domain.UserFeed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminFeedViewAssembler {

  private final FeedRepository feedRepository;
  private final FeedAuthorRepository feedAuthorRepository;
  private final FeedAuthorReader feedAuthorReader;
  private final FeedBackgroundReader feedBackgroundReader;
  private final S3Uploader s3Uploader;

  @Transactional(readOnly = true)
  public AdminOfficialFeedView getOfficialFeedView(long feedId) {
    return feedRepository
        .findOfficialFeedById(feedId)
        .map(this::toOfficialFeedView)
        .orElseThrow(() -> new DeeppleException(FeedErrorCode.FEED_NOT_FOUND, "feedId=" + feedId));
  }

  public AdminUserFeedView toUserFeedView(UserFeed feed) {
    return toUserFeedView(feed, feedAuthorReader.getAuthor(feed.getAuthorId()));
  }

  public AdminUserFeedDetailView toUserFeedDetailView(UserFeed feed) {
    FeedAuthor author = feedAuthorReader.getAuthor(feed.getAuthorId());
    return new AdminUserFeedDetailView(
        feed.getId(),
        feed.getType().name(),
        feed.getStatus().name(),
        author.getMemberId(),
        author.getNickname(),
        feed.getContent(),
        toPollOptions(feed),
        toImageViews(feed),
        feedBackgroundReader.readCatalog().find(feed.getBackgroundId()).orElse(null),
        feed.getCreatedAt(),
        feed.getPublishedAt(),
        feed.getDeletedAt());
  }

  public List<AdminUserFeedView> toUserFeedViews(List<UserFeed> feeds) {
    Map<Long, FeedAuthor> authors =
        feedAuthorRepository.findAllById(feeds.stream().map(Feed::getAuthorId).toList()).stream()
            .collect(Collectors.toMap(FeedAuthor::getId, Function.identity()));
    return feeds.stream()
        .map(feed -> toUserFeedView(feed, authors.get(feed.getAuthorId())))
        .toList();
  }

  public AdminOfficialFeedView toOfficialFeedView(OfficialFeed feed) {
    return new AdminOfficialFeedView(
        feed.getId(),
        feed.getStatus().name(),
        feed.getTitle(),
        feed.getContent(),
        toImageViews(feed),
        feed.getTargetGrades().stream().sorted().toList(),
        feed.isPinned(),
        feed.getCreatedAt(),
        feed.getPublishedAt());
  }

  private AdminUserFeedView toUserFeedView(UserFeed feed, FeedAuthor author) {
    return new AdminUserFeedView(
        feed.getId(),
        feed.getType().name(),
        feed.getStatus().name(),
        author.getNickname(),
        feed.getContent(),
        toImageViews(feed),
        feed.getCreatedAt(),
        feed.getPublishedAt());
  }

  private List<String> toPollOptions(UserFeed feed) {
    if (feed instanceof PollFeed pollFeed) {
      return pollFeed.getOptions().stream().map(FeedPollOption::getContent).toList();
    }
    return List.of();
  }

  private List<FeedImageView> toImageViews(Feed feed) {
    return feed.getImages().stream().map(this::toImageView).toList();
  }

  private FeedImageView toImageView(FeedImage image) {
    return new FeedImageView(
        image.getImageId(),
        s3Uploader.getObjectUrl(image.getImageKey()),
        s3Uploader.getObjectUrl(image.getThumbnailKey()));
  }
}
