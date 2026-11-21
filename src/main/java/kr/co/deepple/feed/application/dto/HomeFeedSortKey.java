package kr.co.deepple.feed.application.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedErrorCode;

public record HomeFeedSortKey(long feedId, long score) {

  public static final HomeFeedSortKey START = new HomeFeedSortKey(Long.MAX_VALUE, Long.MAX_VALUE);

  private static final long PINNED_SCORE_OFFSET = 1L << 52;
  private static final long MAX_SCORE = PINNED_SCORE_OFFSET * 2;
  private static final String DELIMITER = "_";
  private static final int PART_COUNT = 2;

  public HomeFeedSortKey(long feedId, boolean isPinned, LocalDateTime publishedAt) {
    this(feedId, toScore(isPinned, publishedAt));
  }

  public static HomeFeedSortKey of(Feed feed) {
    return new HomeFeedSortKey(feed.getId(), feed.isPinned(), feed.getPublishedAt());
  }

  public static Optional<HomeFeedSortKey> parse(String cursor) {
    if (isEmpty(cursor)) {
      return Optional.empty();
    }
    String[] parts = cursor.split(DELIMITER);
    if (parts.length != PART_COUNT) {
      throw invalidCursorException(cursor);
    }
    HomeFeedSortKey parsed = parseParts(parts, cursor);
    if (parsed.isOutOfRange()) {
      throw invalidCursorException(cursor);
    }
    return Optional.of(parsed);
  }

  public String toCursor() {
    return score + DELIMITER + feedId;
  }

  public boolean isAfter(HomeFeedSortKey other) {
    if (score != other.score) {
      return score < other.score;
    }
    return feedId < other.feedId;
  }

  private static long toScore(boolean isPinned, LocalDateTime publishedAt) {
    long publishedAtMicros =
        ChronoUnit.MICROS.between(Instant.EPOCH, publishedAt.toInstant(ZoneOffset.UTC));
    if (isPinned) {
      return PINNED_SCORE_OFFSET + publishedAtMicros;
    }
    return publishedAtMicros;
  }

  private static boolean isEmpty(String cursor) {
    return cursor == null || cursor.isBlank();
  }

  private static HomeFeedSortKey parseParts(String[] parts, String cursor) {
    try {
      return new HomeFeedSortKey(Long.parseLong(parts[1]), Long.parseLong(parts[0]));
    } catch (NumberFormatException e) {
      throw invalidCursorException(cursor);
    }
  }

  private boolean isOutOfRange() {
    return score < 0L || score >= MAX_SCORE || feedId <= 0L;
  }

  private static DeeppleException invalidCursorException(String cursor) {
    return new DeeppleException(FeedErrorCode.INVALID_FEED_CURSOR, "cursor=" + cursor);
  }
}
