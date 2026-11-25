package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.application.port.out.LikedFeedCacheRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedAuthorType;
import kr.co.deepple.feed.domain.FeedCount;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedBlockFixture;
import kr.co.deepple.feed.fixture.FeedCountFixture;
import kr.co.deepple.feed.fixture.FeedLikeFixture;
import kr.co.deepple.feed.fixture.OfficialFeedFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("피드 좋아요 통합 테스트")
class LikeFeedServiceIntegrationTest extends IntegrationTestSupport {

  private static final long VIEWER_MEMBER_ID = 1L;
  private static final long WRITER_MEMBER_ID = 2L;
  private static final int CONCURRENT_REQUESTS = 10;

  @Autowired private LikeFeedService likeFeedService;

  @Autowired private GetFeedService getFeedService;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedBlockRepository feedBlockRepository;

  @Autowired private FeedLikeRepository feedLikeRepository;

  @Autowired private FeedCountRepository feedCountRepository;

  @Autowired private LikedFeedCacheRepository likedFeedCacheRepository;

  @Autowired private FlushFeedLikeCountsService flushFeedLikeCountsService;

  @Autowired private StringRedisTemplate redisTemplate;

  private FeedAuthor viewer;
  private FeedAuthor writer;

  @BeforeEach
  void setUp() {
    viewer = feedAuthorRepository.save(activeMember(VIEWER_MEMBER_ID, "GOLD"));
    writer = feedAuthorRepository.save(activeMember(WRITER_MEMBER_ID, "GOLD"));
  }

  @Test
  @DisplayName("좋아요하면 기록이 생기고 좋아요 수와 isLiked에 반영된다")
  void likeFeed() {
    // given
    long feedId = saveFeed(writer, "GOLD");
    getFeedService.getFeed(VIEWER_MEMBER_ID, feedId);

    // when
    likeFeedService.likeFeed(VIEWER_MEMBER_ID, feedId);

    // then
    FeedView view = getFeedService.getFeed(VIEWER_MEMBER_ID, feedId);
    assertThat(view.likeCount()).isEqualTo(1L);
    assertThat(view.isLiked()).isTrue();
    assertThat(feedLikeRepository.count()).isEqualTo(1L);
  }

  @Test
  @DisplayName("같은 좋아요를 두 번 보내도 기록과 수는 한 번만 오른다")
  void likeTwice() {
    // given
    long feedId = saveFeed(writer, "GOLD");
    getFeedService.getFeed(VIEWER_MEMBER_ID, feedId);

    // when
    likeFeedService.likeFeed(VIEWER_MEMBER_ID, feedId);
    likeFeedService.likeFeed(VIEWER_MEMBER_ID, feedId);

    // then
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, feedId).likeCount()).isEqualTo(1L);
    assertThat(feedLikeRepository.count()).isEqualTo(1L);
  }

  @Test
  @DisplayName("같은 좋아요가 동시에 와도 기록과 수는 한 번만 오른다")
  void likeConcurrently() throws Exception {
    // given
    long feedId = saveFeed(writer, "GOLD");
    getFeedService.getFeed(VIEWER_MEMBER_ID, feedId);

    // when
    runConcurrently(index -> likeFeedService.likeFeed(VIEWER_MEMBER_ID, feedId));

    // then
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, feedId).likeCount()).isEqualTo(1L);
    assertThat(feedLikeRepository.count()).isEqualTo(1L);
  }

  @Test
  @DisplayName("카운터가 없을 때 여러 회원이 동시에 좋아요해도 수가 한 번씩만 오른다")
  void likeConcurrentlyByManyMembersWithoutCounter() throws Exception {
    // given
    long feedId = saveFeed(writer, "GOLD");
    for (long memberId = 100L; memberId < 100L + CONCURRENT_REQUESTS; memberId++) {
      feedAuthorRepository.save(activeMember(memberId, "GOLD"));
    }
    getFeedService.getFeed(VIEWER_MEMBER_ID, feedId);
    redisTemplate.delete("feed:like-count:v1:" + feedId);

    // when
    runConcurrently(index -> likeFeedService.likeFeed(100L + index, feedId));

    // then
    assertThat(feedLikeRepository.count()).isEqualTo(CONCURRENT_REQUESTS);
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, feedId).likeCount())
        .isEqualTo(CONCURRENT_REQUESTS);
  }

  @Test
  @DisplayName("좋아요 목록을 DB에서 읽어 캐시에 채우는 사이에 좋아요해도 isLiked가 true다")
  void showLikedWhenLikedWhileLoadingLikedFeeds() {
    // given
    long feedId = saveFeed(writer, "GOLD");
    List<Long> loadingLikedFeedIds = feedLikeRepository.findFeedIdsByMemberId(VIEWER_MEMBER_ID);

    // when
    likeFeedService.likeFeed(VIEWER_MEMBER_ID, feedId);
    likedFeedCacheRepository.saveIfAbsent(VIEWER_MEMBER_ID, loadingLikedFeedIds);

    // then
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, feedId).isLiked()).isTrue();
  }

  @Test
  @DisplayName("카운터가 없을 때 좋아요하면 좋아요 기록을 세서 카운터와 feed_counts를 채운다")
  void fillCounterFromLikesOnLike() {
    // given
    long feedId = saveFeed(writer, "GOLD");
    feedLikeRepository.save(FeedLikeFixture.builder().feedId(feedId).memberId(10L).build());
    feedLikeRepository.save(FeedLikeFixture.builder().feedId(feedId).memberId(11L).build());
    feedCountRepository.save(FeedCountFixture.builder().feedId(feedId).likeCount(0L).build());

    // when
    likeFeedService.likeFeed(VIEWER_MEMBER_ID, feedId);
    flushFeedLikeCountsService.flush();

    // then
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, feedId).likeCount()).isEqualTo(3L);
    assertThat(feedCountRepository.findById(feedId))
        .get()
        .extracting(FeedCount::getLikeCount)
        .isEqualTo(3L);
  }

  @Test
  @DisplayName("좋아요로 카운터가 바뀌면 만료 시간을 다시 7일로 늘린다")
  void extendCounterTtlOnLike() {
    // given
    long feedId = saveFeed(writer, "GOLD");
    getFeedService.getFeed(VIEWER_MEMBER_ID, feedId);
    String key = "feed:like-count:v1:" + feedId;
    redisTemplate.expire(key, Duration.ofSeconds(10));

    // when
    likeFeedService.likeFeed(VIEWER_MEMBER_ID, feedId);

    // then
    assertThat(redisTemplate.getExpire(key)).isGreaterThan(Duration.ofDays(6).toSeconds());
  }

  @Test
  @DisplayName("볼 수 없는 피드는 없는 피드로 거절하고 기록을 남기지 않는다")
  void rejectInvisibleFeeds() {
    // given
    FeedAuthor dormant =
        feedAuthorRepository.save(
            FeedAuthorFixture.builder()
                .memberId(3L)
                .activityStatus("DORMANT")
                .grade("GOLD")
                .build());
    FeedAuthor blocked = feedAuthorRepository.save(activeMember(4L, "GOLD"));
    feedBlockRepository.save(
        FeedBlockFixture.builder().blockerId(4L).blockedId(VIEWER_MEMBER_ID).build());
    long deletedFeedId = saveFeed(writer, "GOLD");
    Feed deleted = feedRepository.findById(deletedFeedId).orElseThrow();
    deleted.delete();
    feedRepository.save(deleted);
    List<Long> invisibleFeedIds =
        List.of(
            saveFeed(writer, "SILVER"),
            feedRepository
                .save(
                    TextFeedFixture.builder()
                        .authorId(writer.getId())
                        .grade("GOLD")
                        .image(image())
                        .build())
                .getId(),
            deletedFeedId,
            saveFeed(dormant, "GOLD"),
            saveFeed(blocked, "GOLD"));

    // when & then
    for (long feedId : invisibleFeedIds) {
      assertThatThrownBy(() -> likeFeedService.likeFeed(VIEWER_MEMBER_ID, feedId))
          .isInstanceOf(DeeppleException.class)
          .extracting("errorCode")
          .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    }
    assertThat(feedLikeRepository.count()).isZero();
  }

  @Test
  @DisplayName("관리자 피드와 내 피드도 좋아요할 수 있다")
  void likeOfficialAndMyFeed() {
    // given
    FeedAuthor official =
        feedAuthorRepository.save(FeedAuthorFixture.builder().memberId(99L).build());
    ReflectionTestUtils.setField(official, "authorType", FeedAuthorType.OFFICIAL);
    feedAuthorRepository.save(official);
    long officialFeedId =
        feedRepository
            .save(
                OfficialFeedFixture.builder()
                    .authorId(official.getId())
                    .targetGrades(Set.of())
                    .build())
            .getId();
    long myFeedId = saveFeed(viewer, "GOLD");

    // when
    likeFeedService.likeFeed(VIEWER_MEMBER_ID, officialFeedId);
    likeFeedService.likeFeed(VIEWER_MEMBER_ID, myFeedId);

    // then
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, officialFeedId).isLiked()).isTrue();
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, myFeedId).isLiked()).isTrue();
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

  private long saveFeed(FeedAuthor author, String grade) {
    return feedRepository
        .save(TextFeedFixture.builder().authorId(author.getId()).grade(grade).build())
        .getId();
  }

  private static FeedAuthor activeMember(long memberId, String grade) {
    return FeedAuthorFixture.builder()
        .memberId(memberId)
        .activityStatus("ACTIVE")
        .grade(grade)
        .build();
  }

  private static FeedImage image() {
    return FeedImage.of(1L, "images/members/2/1.webp", "images/members/2/1-thumbnail.webp", 0);
  }

  @FunctionalInterface
  private interface IndexedTask {
    void run(int index);
  }
}
