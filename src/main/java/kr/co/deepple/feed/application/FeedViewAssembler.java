package kr.co.deepple.feed.application;

import java.time.Year;
import java.util.List;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.feed.application.dto.FeedAuthorSnapshot;
import kr.co.deepple.feed.application.dto.FeedAuthorView;
import kr.co.deepple.feed.application.dto.FeedBackgroundView;
import kr.co.deepple.feed.application.dto.FeedCounts;
import kr.co.deepple.feed.application.dto.FeedGridItemView;
import kr.co.deepple.feed.application.dto.FeedImageView;
import kr.co.deepple.feed.application.dto.FeedPollOptionView;
import kr.co.deepple.feed.application.dto.FeedPollView;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.dto.FeedTextPosition;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.domain.FeedPersonalityType;
import kr.co.deepple.feed.domain.FeedType;
import kr.co.deepple.feed.domain.TextPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedViewAssembler {

  private final FeedBackgroundReader feedBackgroundReader;
  private final S3Uploader s3Uploader;

  public FeedView toView(
      FeedSnapshot feed, FeedAuthorSnapshot author, boolean isMine, FeedCounts counts) {
    return new FeedView(
        feed.feedId(),
        feed.type().name(),
        toAuthorView(author),
        feed.content(),
        toTextPosition(feed.textPosition()),
        findBackground(feed.backgroundId()),
        feed.images().stream().map(this::toImageView).toList(),
        findPoll(feed, isMine),
        feed.isPinned(),
        counts.likeCount(),
        false,
        counts.commentCount(),
        isMine,
        feed.isHiddenByReport(),
        feed.createdAt());
  }

  public FeedGridItemView toGridItemView(FeedSnapshot feed, FeedCounts counts) {
    return new FeedGridItemView(
        feed.feedId(),
        feed.type().name(),
        findThumbnailUrl(feed),
        feed.content(),
        toTextPosition(feed.textPosition()),
        counts.likeCount(),
        counts.commentCount(),
        feed.isHiddenByReport());
  }

  private FeedAuthorView toAuthorView(FeedAuthorSnapshot author) {
    return new FeedAuthorView(
        author.authorType().name(),
        author.memberId(),
        author.nickname(),
        author.personalityType(),
        FeedPersonalityType.displayNameOf(author.personalityType()),
        author.profileImageUrl(),
        toAge(author.yearOfBirth()),
        author.gender(),
        author.city(),
        author.mbti());
  }

  private Integer toAge(Integer yearOfBirth) {
    if (yearOfBirth == null) {
      return null;
    }
    return Year.now().getValue() - yearOfBirth + 1;
  }

  private FeedTextPosition toTextPosition(TextPosition position) {
    if (position == null) {
      return null;
    }
    return new FeedTextPosition(position.x(), position.y(), position.rotation(), position.scale());
  }

  private FeedBackgroundView findBackground(Long backgroundId) {
    if (backgroundId == null) {
      return null;
    }
    return feedBackgroundReader.readCatalog().backgrounds().stream()
        .filter(background -> background.backgroundId() == backgroundId)
        .findFirst()
        .orElse(null);
  }

  private FeedImageView toImageView(FeedSnapshot.Image image) {
    return new FeedImageView(
        image.imageId(),
        s3Uploader.getObjectUrl(image.imageKey()),
        s3Uploader.getObjectUrl(image.thumbnailKey()));
  }

  private FeedPollView findPoll(FeedSnapshot feed, boolean isMine) {
    if (feed.type() != FeedType.POLL) {
      return null;
    }
    if (isMine) {
      return toVisibleResultPollView(feed);
    }
    return toHiddenResultPollView(feed);
  }

  private FeedPollView toVisibleResultPollView(FeedSnapshot feed) {
    List<FeedPollOptionView> options =
        feed.pollOptions().stream()
            .map(option -> new FeedPollOptionView(option.optionId(), option.content(), 0))
            .toList();
    return new FeedPollView(options, null, true, 0L);
  }

  private FeedPollView toHiddenResultPollView(FeedSnapshot feed) {
    List<FeedPollOptionView> options =
        feed.pollOptions().stream()
            .map(option -> new FeedPollOptionView(option.optionId(), option.content(), null))
            .toList();
    return new FeedPollView(options, null, false, null);
  }

  private String findThumbnailUrl(FeedSnapshot feed) {
    List<FeedSnapshot.Image> images = feed.images();
    if (images.isEmpty()) {
      return findBackgroundThumbnailUrl(feed.backgroundId());
    }
    return s3Uploader.getObjectUrl(images.getFirst().thumbnailKey());
  }

  private String findBackgroundThumbnailUrl(Long backgroundId) {
    FeedBackgroundView background = findBackground(backgroundId);
    if (background == null) {
      return null;
    }
    return background.thumbnailUrl();
  }
}
