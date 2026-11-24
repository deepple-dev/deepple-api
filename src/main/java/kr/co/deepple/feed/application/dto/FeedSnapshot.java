package kr.co.deepple.feed.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.domain.FeedPollOption;
import kr.co.deepple.feed.domain.FeedType;
import kr.co.deepple.feed.domain.PollFeed;
import kr.co.deepple.feed.domain.TextPosition;
import kr.co.deepple.feed.domain.UserFeed;

public record FeedSnapshot(
    long feedId,
    FeedType type,
    long authorId,
    String content,
    TextPosition textPosition,
    Long backgroundId,
    List<Image> images,
    List<PollOption> pollOptions,
    boolean isPinned,
    boolean isHiddenByReport,
    LocalDateTime createdAt) {

  public record Image(long imageId, String imageKey, String thumbnailKey) {}

  public record PollOption(long optionId, String content) {}

  public static FeedSnapshot from(Feed feed) {
    return new FeedSnapshot(
        feed.getId(),
        feed.getType(),
        feed.getAuthorId(),
        feed.getContent(),
        findTextPosition(feed),
        findBackgroundId(feed),
        feed.getImages().stream().map(FeedSnapshot::toImage).toList(),
        findPollOptions(feed),
        feed.isPinned(),
        feed.isHiddenByReport(),
        feed.getCreatedAt());
  }

  public boolean isWrittenBy(long authorId) {
    return this.authorId == authorId;
  }

  public boolean isPoll() {
    return type == FeedType.POLL;
  }

  public List<Long> pollOptionIds() {
    return pollOptions.stream().map(PollOption::optionId).toList();
  }

  public boolean hasPollOption(long optionId) {
    return pollOptions.stream().anyMatch(option -> option.optionId() == optionId);
  }

  private static TextPosition findTextPosition(Feed feed) {
    if (feed instanceof UserFeed userFeed) {
      return userFeed.getTextPosition();
    }
    return null;
  }

  private static Long findBackgroundId(Feed feed) {
    if (feed instanceof UserFeed userFeed) {
      return userFeed.getBackgroundId();
    }
    return null;
  }

  private static Image toImage(FeedImage image) {
    return new Image(image.getImageId(), image.getImageKey(), image.getThumbnailKey());
  }

  private static List<PollOption> findPollOptions(Feed feed) {
    if (feed instanceof PollFeed pollFeed) {
      return pollFeed.getOptions().stream().map(FeedSnapshot::toPollOption).toList();
    }
    return List.of();
  }

  private static PollOption toPollOption(FeedPollOption option) {
    return new PollOption(option.getId(), option.getContent());
  }
}
