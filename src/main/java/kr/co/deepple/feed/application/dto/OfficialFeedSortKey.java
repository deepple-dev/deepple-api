package kr.co.deepple.feed.application.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.OfficialFeed;

public record OfficialFeedSortKey(boolean isPinned, LocalDateTime publishedAt, long feedId) {

  private static final String DELIMITER = "_";
  private static final int PART_COUNT = 3;
  private static final String PINNED = "true";
  private static final String UNPINNED = "false";

  public static OfficialFeedSortKey of(OfficialFeed feed) {
    return new OfficialFeedSortKey(feed.isPinned(), feed.getPublishedAt(), feed.getId());
  }

  public static Optional<OfficialFeedSortKey> parse(String cursor) {
    if (isEmpty(cursor)) {
      return Optional.empty();
    }
    String[] parts = cursor.split(DELIMITER);
    if (parts.length != PART_COUNT) {
      throw invalidCursorException(cursor);
    }
    OfficialFeedSortKey parsed = parseParts(parts, cursor);
    if (parsed.isOutOfRange()) {
      throw invalidCursorException(cursor);
    }
    return Optional.of(parsed);
  }

  public String toCursor() {
    return isPinned + DELIMITER + publishedAt + DELIMITER + feedId;
  }

  private static boolean isEmpty(String cursor) {
    return cursor == null || cursor.isBlank();
  }

  private static OfficialFeedSortKey parseParts(String[] parts, String cursor) {
    try {
      return new OfficialFeedSortKey(
          parsePinned(parts[0], cursor), LocalDateTime.parse(parts[1]), Long.parseLong(parts[2]));
    } catch (DateTimeParseException | NumberFormatException e) {
      throw invalidCursorException(cursor);
    }
  }

  private static boolean parsePinned(String value, String cursor) {
    if (PINNED.equals(value)) {
      return true;
    }
    if (UNPINNED.equals(value)) {
      return false;
    }
    throw invalidCursorException(cursor);
  }

  private boolean isOutOfRange() {
    return feedId <= 0L;
  }

  private static DeeppleException invalidCursorException(String cursor) {
    return new DeeppleException(FeedErrorCode.INVALID_FEED_CURSOR, "cursor=" + cursor);
  }
}
