package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.port.out.FeedVoteCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteCountRepository;
import kr.co.deepple.feed.domain.FeedType;
import kr.co.deepple.feed.fixture.FeedVoteCountFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("표 수 조회 통합 테스트")
class FeedVoteCountReaderIntegrationTest extends IntegrationTestSupport {

  @Autowired private FeedVoteCountReader feedVoteCountReader;

  @Autowired private FeedVoteCountRepository feedVoteCountRepository;

  @Autowired private FeedVoteCountCacheRepository feedVoteCountCacheRepository;

  @Test
  @DisplayName("카운터가 없으면 feed_vote_counts에서 채우고, 저장되지 않은 선택지는 0이다")
  void fillMissingCountersFromSavedCounts() {
    // given
    feedVoteCountRepository.save(
        FeedVoteCountFixture.builder().optionId(11L).feedId(1L).voteCount(4L).build());

    // when
    Map<Long, Map<Long, Long>> voteCounts =
        feedVoteCountReader.read(List.of(pollFeed(1L, 11L, 12L), pollFeed(2L, 21L, 22L)));

    // then
    assertThat(voteCounts)
        .containsOnly(
            Map.entry(1L, Map.of(11L, 4L, 12L, 0L)), Map.entry(2L, Map.of(21L, 0L, 22L, 0L)));
    assertThat(feedVoteCountCacheRepository.findAll(List.of(1L, 2L))).isEqualTo(voteCounts);
  }

  @Test
  @DisplayName("카운터가 있으면 DB를 읽지 않고 카운터 값을 준다")
  void readCachedCounters() {
    // given
    feedVoteCountCacheRepository.saveAllIfAbsent(Map.of(1L, Map.of(11L, 7L, 12L, 2L)));
    feedVoteCountRepository.save(
        FeedVoteCountFixture.builder().optionId(11L).feedId(1L).voteCount(1L).build());

    // when & then
    assertThat(feedVoteCountReader.read(pollFeed(1L, 11L, 12L)))
        .isEqualTo(Map.of(11L, 7L, 12L, 2L));
  }

  private static FeedSnapshot pollFeed(long feedId, long... optionIds) {
    List<FeedSnapshot.PollOption> options =
        Arrays.stream(optionIds)
            .mapToObj(optionId -> new FeedSnapshot.PollOption(optionId, "선택지" + optionId))
            .toList();
    return new FeedSnapshot(
        feedId, FeedType.POLL, 1L, "질문", null, null, List.of(), options, false, false, null);
  }
}
