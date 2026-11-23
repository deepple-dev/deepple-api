package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedCounts;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import kr.co.deepple.feed.fixture.FeedCountFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("피드 숫자 조회 통합 테스트")
class FeedCountReaderIntegrationTest extends IntegrationTestSupport {

  @Autowired private FeedCountReader feedCountReader;

  @Autowired private FeedCountRepository feedCountRepository;

  @Autowired private FeedLikeCountCacheRepository feedLikeCountCacheRepository;

  @Test
  @DisplayName("카운터가 있으면 카운터 값을, 없으면 feed_counts 값을 주고 feed_counts에도 없으면 0을 준다")
  void readLikeCounts() {
    // given
    feedLikeCountCacheRepository.saveAllIfAbsent(Map.of(1L, 7L));
    feedCountRepository.save(FeedCountFixture.builder().feedId(2L).likeCount(42L).build());

    // when
    Map<Long, FeedCounts> counts = feedCountReader.read(Set.of(1L, 2L, 3L));

    // then
    assertThat(counts.get(1L).likeCount()).isEqualTo(7L);
    assertThat(counts.get(2L).likeCount()).isEqualTo(42L);
    assertThat(counts.get(3L).likeCount()).isZero();
    assertThat(counts.get(1L).commentCount()).isZero();
  }

  @Test
  @DisplayName("feed_counts에서 채운 값은 카운터에 저장해 다음부터 카운터에서 읽는다")
  void fillCounterFromDatabase() {
    // given
    feedCountRepository.save(FeedCountFixture.builder().feedId(1L).likeCount(42L).build());
    feedCountReader.read(1L);
    feedCountRepository.save(FeedCountFixture.builder().feedId(1L).likeCount(0L).build());

    // when
    FeedCounts counts = feedCountReader.read(1L);

    // then
    assertThat(counts.likeCount()).isEqualTo(42L);
    assertThat(feedLikeCountCacheRepository.findAll(List.of(1L))).containsEntry(1L, 42L);
  }
}
