package kr.co.deepple.feed.domain;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public record PollResult(long participantCount, Map<Long, Integer> votePercents) {

  private static final long TOTAL_PERCENT = 100L;

  public static PollResult of(List<Long> optionIds, Map<Long, Long> voteCounts) {
    long participantCount =
        optionIds.stream().mapToLong(optionId -> voteCounts.getOrDefault(optionId, 0L)).sum();
    if (participantCount == 0L) {
      return new PollResult(0L, zeroPercents(optionIds));
    }
    return new PollResult(
        participantCount, toLargestRemainderPercents(optionIds, voteCounts, participantCount));
  }

  public int votePercentOf(long optionId) {
    return votePercents.get(optionId);
  }

  private static Map<Long, Integer> zeroPercents(List<Long> optionIds) {
    Map<Long, Integer> percents = new HashMap<>();
    optionIds.forEach(optionId -> percents.put(optionId, 0));
    return percents;
  }

  private static Map<Long, Integer> toLargestRemainderPercents(
      List<Long> optionIds, Map<Long, Long> voteCounts, long participantCount) {
    List<Share> shares =
        IntStream.range(0, optionIds.size())
            .mapToObj(order ->
                Share.of(
                    optionIds.get(order),
                    order,
                    voteCounts.getOrDefault(optionIds.get(order), 0L),
                    participantCount))
            .toList();
    long missingPercent = TOTAL_PERCENT - shares.stream().mapToLong(Share::flooredPercent).sum();
    List<Share> byRemainder =
        shares.stream()
            .sorted(
                Comparator.comparingLong(Share::remainder)
                    .reversed()
                    .thenComparingInt(Share::order))
            .toList();
    Map<Long, Integer> percents = new HashMap<>();
    for (int rank = 0; rank < byRemainder.size(); rank++) {
      Share share = byRemainder.get(rank);
      percents.put(share.optionId(), share.toPercent(rank < missingPercent));
    }
    return percents;
  }

  private record Share(long optionId, int order, long flooredPercent, long remainder) {

    static Share of(long optionId, int order, long voteCount, long participantCount) {
      long scaled = voteCount * TOTAL_PERCENT;
      return new Share(optionId, order, scaled / participantCount, scaled % participantCount);
    }

    int toPercent(boolean receivesMissingPercent) {
      if (receivesMissingPercent) {
        return (int) flooredPercent + 1;
      }
      return (int) flooredPercent;
    }
  }
}
