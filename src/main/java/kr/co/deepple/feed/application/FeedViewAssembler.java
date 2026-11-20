package kr.co.deepple.feed.application;

import java.time.Year;
import java.util.List;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.feed.application.dto.FeedAuthorView;
import kr.co.deepple.feed.application.dto.FeedBackgroundView;
import kr.co.deepple.feed.application.dto.FeedCounts;
import kr.co.deepple.feed.application.dto.FeedGridItemView;
import kr.co.deepple.feed.application.dto.FeedImageView;
import kr.co.deepple.feed.application.dto.FeedPollOptionView;
import kr.co.deepple.feed.application.dto.FeedPollView;
import kr.co.deepple.feed.application.dto.FeedTextPosition;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.domain.FeedPersonalityType;
import kr.co.deepple.feed.domain.OfficialFeed;
import kr.co.deepple.feed.domain.PollFeed;
import kr.co.deepple.feed.domain.TextPosition;
import kr.co.deepple.feed.domain.UserFeed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedViewAssembler {

  private final FeedBackgroundReader feedBackgroundReader;
  private final S3Uploader s3Uploader;

  public FeedView toView(Feed feed, FeedAuthor author, boolean isMine, FeedCounts counts) {
    return new FeedView(
        feed.getId(),
        feed.getType().name(),
        toAuthorView(author),
        feed.getContent(),
        findTextPosition(feed),
        findBackground(feed),
        feed.getImages().stream().map(this::toImageView).toList(),
        findPoll(feed, isMine),
        isPinned(feed),
        counts.likeCount(),
        false,
        counts.commentCount(),
        isMine,
        feed.isHiddenByReport(),
        feed.getCreatedAt());
  }

  public FeedGridItemView toGridItemView(Feed feed, FeedCounts counts) {
    return new FeedGridItemView(
        feed.getId(),
        feed.getType().name(),
        findThumbnailUrl(feed),
        feed.getContent(),
        findTextPosition(feed),
        counts.likeCount(),
        counts.commentCount(),
        feed.isHiddenByReport());
  }

  private FeedAuthorView toAuthorView(FeedAuthor author) {
    return new FeedAuthorView(
        author.getAuthorType().name(),
        author.getMemberId(),
        author.getNickname(),
        author.getPersonalityType(),
        FeedPersonalityType.displayNameOf(author.getPersonalityType()),
        author.getProfileImageUrl(),
        toAge(author.getYearOfBirth()),
        author.getGender(),
        author.getCity(),
        author.getMbti());
  }

  private Integer toAge(Integer yearOfBirth) {
    return yearOfBirth == null ? null : Year.now().getValue() - yearOfBirth + 1;
  }

  private FeedTextPosition findTextPosition(Feed feed) {
    if (!(feed instanceof UserFeed userFeed)) {
      return null;
    }
    TextPosition position = userFeed.getTextPosition();
    return new FeedTextPosition(position.x(), position.y(), position.rotation(), position.scale());
  }

  private FeedBackgroundView findBackground(Feed feed) {
    if (!(feed instanceof UserFeed userFeed) || userFeed.getBackgroundId() == null) {
      return null;
    }
    long backgroundId = userFeed.getBackgroundId();
    return feedBackgroundReader.readCatalog().backgrounds().stream()
        .filter(background -> background.backgroundId() == backgroundId)
        .findFirst()
        .orElse(null);
  }

  private String findThumbnailUrl(Feed feed) {
    List<FeedImage> images = feed.getImages();
    if (!images.isEmpty()) {
      return s3Uploader.getObjectUrl(images.getFirst().getThumbnailKey());
    }
    FeedBackgroundView background = findBackground(feed);
    return background == null ? null : background.thumbnailUrl();
  }

  private FeedImageView toImageView(FeedImage image) {
    return new FeedImageView(
        image.getImageId(),
        s3Uploader.getObjectUrl(image.getImageKey()),
        s3Uploader.getObjectUrl(image.getThumbnailKey()));
  }

  private FeedPollView findPoll(Feed feed, boolean isMine) {
    if (!(feed instanceof PollFeed pollFeed)) {
      return null;
    }
    List<FeedPollOptionView> options =
        pollFeed.getOptions().stream()
            .map(option ->
                new FeedPollOptionView(option.getId(), option.getContent(), isMine ? 0 : null))
            .toList();
    return new FeedPollView(options, null, isMine, isMine ? 0L : null);
  }

  private boolean isPinned(Feed feed) {
    return feed instanceof OfficialFeed officialFeed && officialFeed.isPinned();
  }
}
