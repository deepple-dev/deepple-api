package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.port.out.FeedLikeRepository;
import kr.co.deepple.feed.domain.FeedLike;
import kr.co.deepple.feed.fixture.FeedLikeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@DisplayName("내가 누른 피드 조회 통합 테스트")
class LikedFeedReaderIntegrationTest extends IntegrationTestSupport {

  private static final long MEMBER_ID = 1L;

  @Autowired private LikedFeedReader likedFeedReader;

  @Autowired private FeedLikeRepository feedLikeRepository;

  @Autowired private StringRedisTemplate redisTemplate;

  @Test
  @DisplayName("캐시가 없으면 DB에서 채우고, 그 뒤로는 캐시로 확인한다")
  void fillCacheFromDatabase() {
    // given
    feedLikeRepository.save(FeedLikeFixture.builder().feedId(10L).memberId(MEMBER_ID).build());
    feedLikeRepository.save(FeedLikeFixture.builder().feedId(20L).memberId(2L).build());
    likedFeedReader.findLikedFeedIds(MEMBER_ID, Set.of(10L));
    feedLikeRepository.deleteAll();

    // when & then
    assertThat(likedFeedReader.findLikedFeedIds(MEMBER_ID, Set.of(10L, 20L, 30L)))
        .containsExactly(10L);
  }

  @Test
  @DisplayName("누른 피드가 없는 회원도 캐시해 DB를 다시 읽지 않는다")
  void cacheEmptyLikes() {
    // given
    likedFeedReader.findLikedFeedIds(MEMBER_ID, Set.of(10L));
    feedLikeRepository.save(FeedLikeFixture.builder().feedId(10L).memberId(MEMBER_ID).build());

    // when & then
    assertThat(likedFeedReader.isLiked(MEMBER_ID, 10L)).isFalse();
  }

  @Test
  @DisplayName("누른 피드가 많아도 모두 캐시하고 1시간 뒤 만료되게 저장한다")
  void cacheManyLikesWithTtl() {
    // given
    List<FeedLike> likes =
        LongStream.rangeClosed(1L, 2500L)
            .mapToObj(
                feedId -> FeedLikeFixture.builder().feedId(feedId).memberId(MEMBER_ID).build())
            .toList();
    feedLikeRepository.saveAll(likes);

    // when
    likedFeedReader.findLikedFeedIds(MEMBER_ID, Set.of(1L));

    // then
    String key = "feed:liked:v1:" + MEMBER_ID;
    assertThat(redisTemplate.opsForSet().size(key)).isEqualTo(2501L);
    assertThat(redisTemplate.getExpire(key))
        .isPositive()
        .isLessThanOrEqualTo(Duration.ofHours(1).toSeconds());
  }
}
