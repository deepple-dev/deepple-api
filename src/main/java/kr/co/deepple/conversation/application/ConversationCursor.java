package kr.co.deepple.conversation.application;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.conversation.domain.ConversationErrorCode;

record ConversationCursor(long activityMillis, long memberId)
    implements Comparable<ConversationCursor> {

  private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
  private static final String DELIMITER = "_";

  static ConversationCursor of(LocalDateTime lastActivityAt, long memberId) {
    return new ConversationCursor(lastActivityAt.atZone(ZONE).toInstant().toEpochMilli(), memberId);
  }

  static Optional<ConversationCursor> parse(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return Optional.empty();
    }
    String[] parts = cursor.split(DELIMITER);
    if (parts.length != 2) {
      throw invalid(cursor);
    }
    try {
      return Optional.of(
          new ConversationCursor(Long.parseLong(parts[0]), Long.parseLong(parts[1])));
    } catch (NumberFormatException e) {
      throw invalid(cursor);
    }
  }

  String toCursor() {
    return activityMillis + DELIMITER + memberId;
  }

  @Override
  public int compareTo(ConversationCursor other) {
    int byTime = Long.compare(other.activityMillis, activityMillis);
    if (byTime != 0) {
      return byTime;
    }
    return Long.compare(other.memberId, memberId);
  }

  private static DeeppleException invalid(String cursor) {
    return new DeeppleException(
        ConversationErrorCode.INVALID_CONVERSATION_CURSOR, "cursor=" + cursor);
  }
}
