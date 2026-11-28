package kr.co.deepple.feed.application;

import java.time.Year;
import java.util.List;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.feed.application.dto.FeedAuthorSnapshot;
import kr.co.deepple.feed.application.dto.FeedAuthorView;
import kr.co.deepple.feed.application.dto.FeedBackgroundView;
import kr.co.deepple.feed.application.dto.FeedCommentDisplayStatus;
import kr.co.deepple.feed.application.dto.FeedCommentView;
import kr.co.deepple.feed.application.dto.FeedCommentWithAuthor;
import kr.co.deepple.feed.application.dto.FeedCounts;
import kr.co.deepple.feed.application.dto.FeedGridItemView;
import kr.co.deepple.feed.application.dto.FeedImageView;
import kr.co.deepple.feed.application.dto.FeedPollOptionView;
import kr.co.deepple.feed.application.dto.FeedPollView;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.dto.FeedTextPosition;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.dto.OfficialFeedGridItemView;
import kr.co.deepple.feed.application.dto.PollVotes;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedComment;
import kr.co.deepple.feed.domain.FeedPersonalityType;
import kr.co.deepple.feed.domain.PollResult;
import kr.co.deepple.feed.domain.TextPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedViewAssembler {

  private final FeedBackgroundReader feedBackgroundReader;
  private final S3Uploader s3Uploader;

  public FeedView toView(
      FeedSnapshot feed,
      FeedAuthorSnapshot author,
      boolean isMine,
      boolean isLiked,
      FeedCounts counts,
      PollVotes pollVotes) {
    return new FeedView(
        feed.feedId(),
        feed.type().name(),
        toAuthorView(author),
        feed.content(),
        toTextPosition(feed.textPosition()),
        findBackground(feed.backgroundId()),
        feed.images().stream().map(this::toImageView).toList(),
        findPoll(feed, pollVotes),
        feed.isPinned(),
        counts.likeCount(),
        isLiked,
        counts.commentCount(),
        isMine,
        feed.isHiddenByReport(),
        feed.createdAt());
  }

  public FeedPollView toPollView(FeedSnapshot pollFeed, PollVotes pollVotes) {
    if (pollVotes.isResultVisible()) {
      return toVisibleResultPollView(pollFeed, pollVotes);
    }
    return toHiddenResultPollView(pollFeed);
  }

  public FeedGridItemView toGridItemView(FeedSnapshot feed, FeedCounts counts) {
    return new FeedGridItemView(
        feed.feedId(),
        feed.type().name(),
        findThumbnailUrl(feed),
        feed.content(),
        feed.pollOptions().stream().map(FeedSnapshot.PollOption::content).toList(),
        toTextPosition(feed.textPosition()),
        counts.likeCount(),
        counts.commentCount(),
        feed.isHiddenByReport());
  }

  public OfficialFeedGridItemView toOfficialGridItemView(FeedSnapshot feed, FeedCounts counts) {
    return new OfficialFeedGridItemView(
        feed.feedId(),
        findThumbnailUrl(feed),
        feed.content(),
        counts.likeCount(),
        counts.commentCount());
  }

  public FeedCommentView toCommentView(
      FeedCommentWithAuthor commentWithAuthor, long replyCount, FeedAuthor me) {
    FeedComment comment = commentWithAuthor.comment();
    FeedAuthor author = commentWithAuthor.author();
    FeedCommentDisplayStatus displayStatus = decideDisplayStatus(comment, author);
    boolean isVisible = displayStatus == FeedCommentDisplayStatus.VISIBLE;
    return new FeedCommentView(
        comment.getId(),
        comment.getParentId(),
        displayStatus.name(),
        isVisible ? toAuthorView(FeedAuthorSnapshot.from(author)) : null,
        isVisible ? comment.getContent() : null,
        Math.toIntExact(replyCount),
        comment.isWrittenBy(me.getId()),
        comment.getCreatedAt());
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

  private FeedCommentDisplayStatus decideDisplayStatus(FeedComment comment, FeedAuthor author) {
    if (comment.isDeleted()) {
      return FeedCommentDisplayStatus.DELETED;
    }
    if (comment.isHiddenByReport()) {
      return FeedCommentDisplayStatus.HIDDEN;
    }
    if (!author.isActive()) {
      return FeedCommentDisplayStatus.INACTIVE_AUTHOR;
    }
    return FeedCommentDisplayStatus.VISIBLE;
  }

  /** 연 나이(올해 − 태어난 해). */
  private Integer toAge(Integer yearOfBirth) {
    if (yearOfBirth == null) {
      return null;
    }
    return Year.now().getValue() - yearOfBirth;
  }

  private FeedTextPosition toTextPosition(TextPosition position) {
    if (position == null) {
      return null;
    }
    return new FeedTextPosition(position.x(), position.y(), position.rotation(), position.scale());
  }

  private FeedBackgroundView findBackground(Long backgroundId) {
    return feedBackgroundReader.readCatalog().find(backgroundId).orElse(null);
  }

  private FeedImageView toImageView(FeedSnapshot.Image image) {
    return new FeedImageView(
        image.imageId(),
        s3Uploader.getObjectUrl(image.imageKey()),
        s3Uploader.getObjectUrl(image.thumbnailKey()));
  }

  private FeedPollView findPoll(FeedSnapshot feed, PollVotes pollVotes) {
    if (!feed.isPoll()) {
      return null;
    }
    return toPollView(feed, pollVotes);
  }

  private FeedPollView toVisibleResultPollView(FeedSnapshot pollFeed, PollVotes pollVotes) {
    PollResult result = PollResult.of(pollFeed.pollOptionIds(), pollVotes.voteCounts());
    List<FeedPollOptionView> options =
        pollFeed.pollOptions().stream()
            .map(option ->
                new FeedPollOptionView(
                    option.optionId(), option.content(), result.votePercentOf(option.optionId())))
            .toList();
    return new FeedPollView(options, pollVotes.myOptionId(), true, result.participantCount());
  }

  private FeedPollView toHiddenResultPollView(FeedSnapshot pollFeed) {
    List<FeedPollOptionView> options =
        pollFeed.pollOptions().stream()
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
