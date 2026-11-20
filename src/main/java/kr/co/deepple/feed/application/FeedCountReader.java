package kr.co.deepple.feed.application;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.dto.FeedCounts;
import org.springframework.stereotype.Component;

@Component
public class FeedCountReader {

  public Map<Long, FeedCounts> read(Collection<Long> feedIds) {
    return feedIds.stream()
        .distinct()
        .collect(Collectors.toMap(Function.identity(), feedId -> FeedCounts.EMPTY));
  }

  public FeedCounts read(long feedId) {
    return FeedCounts.EMPTY;
  }
}
