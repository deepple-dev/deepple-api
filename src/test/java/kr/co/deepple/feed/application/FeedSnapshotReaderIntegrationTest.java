package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedAuthorSnapshot;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedSnapshotCacheRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("피드 스냅샷 읽기 통합 테스트")
class FeedSnapshotReaderIntegrationTest extends IntegrationTestSupport {

  private static final String NEW_PROFILE_IMAGE_URL = "https://example.com/new.jpg";

  @Autowired private FeedSnapshotReader feedSnapshotReader;

  @Autowired private FeedSnapshotLoader feedSnapshotLoader;

  @Autowired private FeedSnapshotCacheRepository feedSnapshotCacheRepository;

  @Autowired private SyncHomeFeedCacheService syncHomeFeedCacheService;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Test
  @DisplayName("작성자를 DB에서 읽는 사이에 정보가 바뀌면 옛 스냅샷을 저장하지 않는다")
  void doNotSaveStaleAuthorLoadedBeforeChange() {
    // given
    FeedAuthor author = feedAuthorRepository.save(FeedAuthorFixture.builder().build());
    List<FeedAuthorSnapshot> staleAuthors = feedSnapshotLoader.loadAuthors(List.of(author.getId()));
    author.updateProfileImage(NEW_PROFILE_IMAGE_URL, Instant.now());
    feedAuthorRepository.save(author);
    syncHomeFeedCacheService.evictAuthor(author.getId());

    // when
    feedSnapshotCacheRepository.saveAuthors(staleAuthors);

    // then
    assertThat(feedSnapshotReader.readAuthors(List.of(author.getId())).get(author.getId()))
        .extracting(FeedAuthorSnapshot::profileImageUrl)
        .isEqualTo(NEW_PROFILE_IMAGE_URL);
  }
}
