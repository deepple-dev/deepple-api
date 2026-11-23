package kr.co.deepple.feed.application;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.port.out.FeedLikeRepository;
import kr.co.deepple.feed.application.port.out.LikedFeedCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikedFeedReader {

  private final LikedFeedCacheRepository likedFeedCacheRepository;
  private final FeedLikeRepository feedLikeRepository;

  public Set<Long> findLikedFeedIds(long memberId, Set<Long> feedIds) {
    if (feedIds.isEmpty()) {
      return Set.of();
    }
    Optional<Set<Long>> cached = likedFeedCacheRepository.findLikedFeedIds(memberId, feedIds);
    if (cached.isPresent()) {
      return cached.get();
    }
    Set<Long> likedFeedIds = loadLikedFeedIds(memberId);
    return feedIds.stream().filter(likedFeedIds::contains).collect(Collectors.toSet());
  }

  public boolean isLiked(long memberId, long feedId) {
    return findLikedFeedIds(memberId, Set.of(feedId)).contains(feedId);
  }

  private Set<Long> loadLikedFeedIds(long memberId) {
    List<Long> likedFeedIds = feedLikeRepository.findFeedIdsByMemberId(memberId);
    likedFeedCacheRepository.save(memberId, likedFeedIds);
    return Set.copyOf(likedFeedIds);
  }
}
