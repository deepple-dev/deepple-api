package kr.co.deepple.feed.application;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kr.co.deepple.feed.application.dto.FeedAuthorSnapshot;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.port.out.FeedSnapshotCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedSnapshotReader {

  private final FeedSnapshotCacheRepository feedSnapshotCacheRepository;
  private final FeedSnapshotLoader feedSnapshotLoader;

  public Map<Long, FeedSnapshot> readPublishedFeeds(Collection<Long> feedIds) {
    List<FeedSnapshot> cached = feedSnapshotCacheRepository.findFeeds(feedIds);
    Set<Long> missingIds = findMissingIds(feedIds, cached, FeedSnapshot::feedId);
    if (missingIds.isEmpty()) {
      return toMap(cached, FeedSnapshot::feedId);
    }
    List<FeedSnapshot> loaded = feedSnapshotLoader.loadPublishedFeeds(missingIds);
    feedSnapshotCacheRepository.saveFeeds(loaded);
    return toMap(Stream.concat(cached.stream(), loaded.stream()).toList(), FeedSnapshot::feedId);
  }

  public Map<Long, FeedAuthorSnapshot> readAuthors(Collection<Long> authorIds) {
    List<FeedAuthorSnapshot> cached = feedSnapshotCacheRepository.findAuthors(authorIds);
    Set<Long> missingIds = findMissingIds(authorIds, cached, FeedAuthorSnapshot::authorId);
    if (missingIds.isEmpty()) {
      return toMap(cached, FeedAuthorSnapshot::authorId);
    }
    List<FeedAuthorSnapshot> loaded = feedSnapshotLoader.loadAuthors(missingIds);
    feedSnapshotCacheRepository.saveAuthors(loaded);
    return toMap(
        Stream.concat(cached.stream(), loaded.stream()).toList(), FeedAuthorSnapshot::authorId);
  }

  private <T> Set<Long> findMissingIds(
      Collection<Long> ids, List<T> cached, Function<T, Long> idGetter) {
    Set<Long> cachedIds = cached.stream().map(idGetter).collect(Collectors.toSet());
    return ids.stream().filter(id -> !cachedIds.contains(id)).collect(Collectors.toSet());
  }

  private <T> Map<Long, T> toMap(List<T> snapshots, Function<T, Long> idGetter) {
    return snapshots.stream().collect(Collectors.toMap(idGetter, Function.identity()));
  }
}
