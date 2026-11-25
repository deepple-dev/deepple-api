package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.application.port.out.LikedFeedCacheRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedCount;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedCountFixture;
import kr.co.deepple.feed.fixture.FeedLikeFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@DisplayName("피드 좋아요 취소 통합 테스트")
class UnlikeFeedServiceIntegrationTest extends IntegrationTestSupport {

  private static final long VIEWER_MEMBER_ID = 1L;
  private static final long WRITER_MEMBER_ID = 2L;
  private static final long OTHER_MEMBER_ID = 3L;
  private static final int CONCURRENT_REQUESTS = 10;

  @Autowired private UnlikeFeedService unlikeFeedService;

  @Autowired private LikeFeedService likeFeedService;

  @Autowired private GetFeedService getFeedService;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedLikeRepository feedLikeRepository;

  @Autowired private FeedCountRepository feedCountRepository;

  @Autowired private FlushFeedLikeCountsService flushFeedLikeCountsService;

  @Autowired private StringRedisTemplate redisTemplate;

  @Autowired private LikedFeedCacheRepository likedFeedCacheRepository;

  private FeedAuthor writer;

  @BeforeEach
  void setUp() {
    feedAuthorRepository.save(activeMember(VIEWER_MEMBER_ID, "GOLD"));
    feedAuthorRepository.save(activeMember(OTHER_MEMBER_ID, "GOLD"));
    writer = feedAuthorRepository.save(activeMember(WRITER_MEMBER_ID, "GOLD"));
  }

  @Test
  @DisplayName("취소하면 내 기록만 지우고 좋아요 수와 isLiked에 반영된다")
  void unlikeFeed() {
    // given
    long feedId = saveFeed("GOLD");
    getFeedService.getFeed(VIEWER_MEMBER_ID, feedId);
    likeFeedService.likeFeed(VIEWER_MEMBER_ID, feedId);
    likeFeedService.likeFeed(OTHER_MEMBER_ID, feedId);

    // when
    unlikeFeedService.unlikeFeed(VIEWER_MEMBER_ID, feedId);

    // then
    FeedView view = getFeedService.getFeed(VIEWER_MEMBER_ID, feedId);
    assertThat(view.likeCount()).isEqualTo(1L);
    assertThat(view.isLiked()).isFalse();
    assertThat(feedLikeRepository.findAll())
        .extracting("memberId")
        .containsExactly(OTHER_MEMBER_ID);
  }

  @Test
  @DisplayName("누르지 않은 피드를 취소하거나 두 번 취소해도 수는 바뀌지 않는다")
  void unlikeWithoutLike() {
    // given
    long feedId = saveFeed("GOLD");
    getFeedService.getFeed(VIEWER_MEMBER_ID, feedId);
    likeFeedService.likeFeed(OTHER_MEMBER_ID, feedId);

    // when
    unlikeFeedService.unlikeFeed(VIEWER_MEMBER_ID, feedId);
    unlikeFeedService.unlikeFeed(VIEWER_MEMBER_ID, feedId);

    // then
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, feedId).likeCount()).isEqualTo(1L);
  }

  @Test
  @DisplayName("같은 취소가 동시에 와도 수는 한 번만 내려간다")
  void unlikeConcurrently() throws Exception {
    // given
    long feedId = saveFeed("GOLD");
    getFeedService.getFeed(VIEWER_MEMBER_ID, feedId);
    likeFeedService.likeFeed(VIEWER_MEMBER_ID, feedId);
    likeFeedService.likeFeed(OTHER_MEMBER_ID, feedId);

    // when
    runConcurrently(index -> unlikeFeedService.unlikeFeed(VIEWER_MEMBER_ID, feedId));

    // then
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, feedId).likeCount()).isEqualTo(1L);
  }

  @Test
  @DisplayName("카운터가 없을 때 여러 회원이 동시에 취소해도 수가 한 번씩만 내려간다")
  void unlikeConcurrentlyByManyMembersWithoutCounter() throws Exception {
    // given
    long feedId = saveFeed("GOLD");
    feedLikeRepository.save(
        FeedLikeFixture.builder().feedId(feedId).memberId(OTHER_MEMBER_ID).build());
    for (long memberId = 100L; memberId < 100L + CONCURRENT_REQUESTS; memberId++) {
      feedAuthorRepository.save(activeMember(memberId, "GOLD"));
      feedLikeRepository.save(FeedLikeFixture.builder().feedId(feedId).memberId(memberId).build());
    }
    getFeedService.getFeed(VIEWER_MEMBER_ID, feedId);
    redisTemplate.delete("feed:like-count:v1:" + feedId);

    // when
    runConcurrently(index -> unlikeFeedService.unlikeFeed(100L + index, feedId));

    // then
    assertThat(feedLikeRepository.findAll())
        .extracting("memberId")
        .containsExactly(OTHER_MEMBER_ID);
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, feedId).likeCount()).isEqualTo(1L);
  }

  @Test
  @DisplayName("좋아요 목록을 DB에서 읽어 캐시에 채우는 사이에 취소해도 isLiked가 false다")
  void showUnlikedWhenUnlikedWhileLoadingLikedFeeds() {
    // given
    long feedId = saveFeed("GOLD");
    feedLikeRepository.save(
        FeedLikeFixture.builder().feedId(feedId).memberId(VIEWER_MEMBER_ID).build());
    List<Long> loadingLikedFeedIds = feedLikeRepository.findFeedIdsByMemberId(VIEWER_MEMBER_ID);

    // when
    unlikeFeedService.unlikeFeed(VIEWER_MEMBER_ID, feedId);
    likedFeedCacheRepository.saveIfAbsent(VIEWER_MEMBER_ID, loadingLikedFeedIds);

    // then
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, feedId).isLiked()).isFalse();
  }

  @Test
  @DisplayName("카운터가 없을 때 취소하면 좋아요 기록을 세서 카운터와 feed_counts를 채운다")
  void fillCounterFromLikesOnUnlike() {
    // given
    long feedId = saveFeed("GOLD");
    feedLikeRepository.save(
        FeedLikeFixture.builder().feedId(feedId).memberId(VIEWER_MEMBER_ID).build());
    feedLikeRepository.save(FeedLikeFixture.builder().feedId(feedId).memberId(10L).build());
    feedLikeRepository.save(FeedLikeFixture.builder().feedId(feedId).memberId(11L).build());
    feedCountRepository.save(FeedCountFixture.builder().feedId(feedId).likeCount(5L).build());

    // when
    unlikeFeedService.unlikeFeed(VIEWER_MEMBER_ID, feedId);
    flushFeedLikeCountsService.flush();

    // then
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, feedId).likeCount()).isEqualTo(2L);
    assertThat(feedCountRepository.findById(feedId))
        .get()
        .extracting(FeedCount::getLikeCount)
        .isEqualTo(2L);
  }

  @Test
  @DisplayName("볼 수 없는 피드는 없는 피드로 거절하고 기록을 지우지 않는다")
  void rejectInvisibleFeed() {
    // given
    long feedId = saveFeed("SILVER");
    feedLikeRepository.save(
        FeedLikeFixture.builder().feedId(feedId).memberId(VIEWER_MEMBER_ID).build());

    // when & then
    assertThatThrownBy(() -> unlikeFeedService.unlikeFeed(VIEWER_MEMBER_ID, feedId))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    assertThat(feedLikeRepository.count()).isEqualTo(1L);
  }

  private void runConcurrently(IndexedTask task) throws Exception {
    CountDownLatch start = new CountDownLatch(1);
    try (ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS)) {
      List<Future<?>> futures = new ArrayList<>();
      for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
        int index = i;
        futures.add(
            executor.submit(() -> {
              start.await();
              task.run(index);
              return null;
            }));
      }
      start.countDown();
      for (Future<?> future : futures) {
        future.get();
      }
    }
  }

  private long saveFeed(String grade) {
    return feedRepository
        .save(TextFeedFixture.builder().authorId(writer.getId()).grade(grade).build())
        .getId();
  }

  private static FeedAuthor activeMember(long memberId, String grade) {
    return FeedAuthorFixture.builder()
        .memberId(memberId)
        .activityStatus("ACTIVE")
        .grade(grade)
        .build();
  }

  @FunctionalInterface
  private interface IndexedTask {
    void run(int index);
  }
}
