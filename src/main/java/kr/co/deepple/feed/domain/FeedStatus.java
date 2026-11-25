package kr.co.deepple.feed.domain;

import java.util.Arrays;
import kr.co.deepple.common.exception.DeeppleException;

public enum FeedStatus {
  PENDING,
  PUBLISHED,
  REJECTED,
  HIDDEN,
  HIDDEN_BY_REPORT;

  public static FeedStatus from(String value) {
    if (value == null) {
      return null;
    }
    return Arrays.stream(values())
        .filter(status -> status.name().equals(value))
        .findFirst()
        .orElseThrow(
            () -> new DeeppleException(FeedErrorCode.INVALID_FEED_STATUS, "status=" + value));
  }
}
