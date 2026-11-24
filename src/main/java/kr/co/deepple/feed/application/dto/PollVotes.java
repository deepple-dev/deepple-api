package kr.co.deepple.feed.application.dto;

import java.util.Map;

public record PollVotes(Long myOptionId, boolean isResultVisible, Map<Long, Long> voteCounts) {

  private static final PollVotes HIDDEN = new PollVotes(null, false, Map.of());

  public static PollVotes hidden() {
    return HIDDEN;
  }

  public static PollVotes visible(Long myOptionId, Map<Long, Long> voteCounts) {
    return new PollVotes(myOptionId, true, voteCounts);
  }
}
