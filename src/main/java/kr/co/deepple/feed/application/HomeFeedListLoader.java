package kr.co.deepple.feed.application;

import java.util.List;
import java.util.stream.Stream;
import kr.co.deepple.common.lock.DistributedLockTemplate;
import kr.co.deepple.feed.application.dto.HomeFeedSortKey;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.application.port.out.HomeFeedCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HomeFeedListLoader {

  private static final String LOCK_KEY_PREFIX = "feed:home:";

  private final HomeFeedCacheRepository homeFeedCacheRepository;
  private final FeedRepository feedRepository;
  private final DistributedLockTemplate distributedLockTemplate;

  public void loadIfNotLoaded(String grade) {
    if (homeFeedCacheRepository.isLoaded(grade)) {
      return;
    }
    distributedLockTemplate.execute(LOCK_KEY_PREFIX + grade, () -> loadIfStillNotLoaded(grade));
  }

  private void loadIfStillNotLoaded(String grade) {
    if (homeFeedCacheRepository.isLoaded(grade)) {
      return;
    }
    homeFeedCacheRepository.replace(grade, findSortKeys(grade));
  }

  private List<HomeFeedSortKey> findSortKeys(String grade) {
    return Stream.concat(
            feedRepository.findHomeUserFeedSortKeys(grade).stream(),
            feedRepository.findHomeOfficialFeedSortKeys(grade).stream())
        .toList();
  }
}
