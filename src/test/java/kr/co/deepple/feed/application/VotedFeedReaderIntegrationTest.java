package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.LongStream;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.port.out.FeedVoteRepository;
import kr.co.deepple.feed.application.port.out.VotedFeedCacheRepository;
import kr.co.deepple.feed.domain.FeedVote;
import kr.co.deepple.feed.fixture.FeedVoteFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@DisplayName("내가 고른 선택지 조회 통합 테스트")
class VotedFeedReaderIntegrationTest extends IntegrationTestSupport {

  private static final long MEMBER_ID = 1L;

  @Autowired private VotedFeedReader votedFeedReader;

  @Autowired private FeedVoteRepository feedVoteRepository;

  @Autowired private VotedFeedCacheRepository votedFeedCacheRepository;

  @Autowired private StringRedisTemplate redisTemplate;

  @Test
  @DisplayName("캐시가 없으면 DB에서 채우고, 그 뒤로는 캐시로 확인한다")
  void fillCacheFromDatabase() {
    // given
    feedVoteRepository.save(
        FeedVoteFixture.builder().feedId(10L).memberId(MEMBER_ID).optionId(101L).build());
    feedVoteRepository.save(
        FeedVoteFixture.builder().feedId(20L).memberId(2L).optionId(201L).build());
    votedFeedReader.findVotedOptionIds(MEMBER_ID, Set.of(10L));
    feedVoteRepository.deleteAll();

    // when & then
    assertThat(votedFeedReader.findVotedOptionIds(MEMBER_ID, Set.of(10L, 20L, 30L)))
        .containsOnly(Map.entry(10L, 101L));
  }

  @Test
  @DisplayName("투표한 피드가 없는 회원도 캐시해 DB를 다시 읽지 않는다")
  void cacheEmptyVotes() {
    // given
    votedFeedReader.findVotedOptionIds(MEMBER_ID, Set.of(10L));
    feedVoteRepository.save(
        FeedVoteFixture.builder().feedId(10L).memberId(MEMBER_ID).optionId(101L).build());

    // when & then
    assertThat(votedFeedReader.findVotedOptionIds(MEMBER_ID, Set.of(10L))).isEmpty();
  }

  @Test
  @DisplayName("캐시가 있을 때만 새 투표를 더한다")
  void addOnlyWhenCached() {
    // given
    votedFeedCacheRepository.addIfPresent(MEMBER_ID, 10L, 101L);
    votedFeedReader.findVotedOptionIds(MEMBER_ID, Set.of(10L));

    // when
    votedFeedCacheRepository.addIfPresent(MEMBER_ID, 20L, 201L);

    // then
    assertThat(votedFeedReader.findVotedOptionIds(MEMBER_ID, Set.of(10L, 20L)))
        .containsOnly(Map.entry(20L, 201L));
  }

  @Test
  @DisplayName("투표한 피드가 많아도 모두 캐시하고 1시간 뒤 만료되게 저장한다")
  void cacheManyVotesWithTtl() {
    // given
    List<FeedVote> votes =
        LongStream.rangeClosed(1L, 2500L)
            .mapToObj(feedId ->
                FeedVoteFixture.builder()
                    .feedId(feedId)
                    .memberId(MEMBER_ID)
                    .optionId(feedId * 10)
                    .build())
            .toList();
    feedVoteRepository.saveAll(votes);

    // when
    votedFeedReader.findVotedOptionIds(MEMBER_ID, Set.of(1L));

    // then
    String key = "feed:voted:v1:" + MEMBER_ID;
    assertThat(redisTemplate.opsForHash().size(key)).isEqualTo(2501L);
    assertThat(redisTemplate.getExpire(key))
        .isPositive()
        .isLessThanOrEqualTo(Duration.ofHours(1).toSeconds());
  }
}
