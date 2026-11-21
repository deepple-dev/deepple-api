package kr.co.deepple.feed.application;

import java.util.Collection;
import java.util.List;
import kr.co.deepple.feed.application.dto.FeedAuthorSnapshot;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.FeedStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FeedSnapshotLoader {

  private final FeedRepository feedRepository;
  private final FeedAuthorRepository feedAuthorRepository;

  @Transactional(readOnly = true)
  public List<FeedSnapshot> loadPublishedFeeds(Collection<Long> feedIds) {
    return feedRepository
        .findByIdInAndStatusAndDeletedAtIsNull(feedIds, FeedStatus.PUBLISHED)
        .stream()
        .map(FeedSnapshot::from)
        .toList();
  }

  public List<FeedAuthorSnapshot> loadAuthors(Collection<Long> authorIds) {
    return feedAuthorRepository.findAllById(authorIds).stream()
        .map(FeedAuthorSnapshot::from)
        .toList();
  }
}
