package kr.co.deepple.feed.application.dto;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.domain.FeedComment;
import kr.co.deepple.feed.domain.FeedErrorCode;

public record FeedCommentCursor(long commentId) {

  public static FeedCommentCursor of(FeedComment comment) {
    return new FeedCommentCursor(comment.getId());
  }

  public static Optional<FeedCommentCursor> parse(String cursor) {
    if (isEmpty(cursor)) {
      return Optional.empty();
    }
    FeedCommentCursor parsed = parseCommentId(cursor);
    if (parsed.isOutOfRange()) {
      throw invalidCursorException(cursor);
    }
    return Optional.of(parsed);
  }

  public String toCursor() {
    return String.valueOf(commentId);
  }

  private static boolean isEmpty(String cursor) {
    return cursor == null || cursor.isBlank();
  }

  private static FeedCommentCursor parseCommentId(String cursor) {
    try {
      return new FeedCommentCursor(Long.parseLong(cursor));
    } catch (NumberFormatException e) {
      throw invalidCursorException(cursor);
    }
  }

  private boolean isOutOfRange() {
    return commentId <= 0L;
  }

  private static DeeppleException invalidCursorException(String cursor) {
    return new DeeppleException(FeedErrorCode.INVALID_FEED_CURSOR, "cursor=" + cursor);
  }
}
