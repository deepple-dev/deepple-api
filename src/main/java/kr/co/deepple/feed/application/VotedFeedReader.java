package kr.co.deepple.feed.application;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.dto.VotedOption;
import kr.co.deepple.feed.application.port.out.FeedVoteRepository;
import kr.co.deepple.feed.application.port.out.VotedFeedCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VotedFeedReader {

  private final VotedFeedCacheRepository votedFeedCacheRepository;
  private final FeedVoteRepository feedVoteRepository;

  public Map<Long, Long> findVotedOptionIds(long memberId, Set<Long> feedIds) {
    if (feedIds.isEmpty()) {
      return Map.of();
    }
    Optional<Map<Long, Long>> cached =
        votedFeedCacheRepository.findVotedOptionIds(memberId, feedIds);
    if (cached.isPresent()) {
      return cached.get();
    }
    Map<Long, Long> votedOptionIds = loadVotedOptionIds(memberId);
    return feedIds.stream()
        .filter(votedOptionIds::containsKey)
        .collect(Collectors.toMap(Function.identity(), votedOptionIds::get));
  }

  public void loadIfAbsent(long memberId) {
    if (!votedFeedCacheRepository.exists(memberId)) {
      loadVotedOptionIds(memberId);
    }
  }

  private Map<Long, Long> loadVotedOptionIds(long memberId) {
    Map<Long, Long> votedOptionIds =
        feedVoteRepository.findVotedOptionsByMemberId(memberId).stream()
            .collect(Collectors.toMap(VotedOption::feedId, VotedOption::optionId));
    votedFeedCacheRepository.saveIfAbsent(memberId, votedOptionIds);
    return votedOptionIds;
  }
}
