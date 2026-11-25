package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedPollOptionView;
import kr.co.deepple.feed.application.dto.FeedPollView;
import kr.co.deepple.feed.application.dto.VotedOption;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteCountRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteRepository;
import kr.co.deepple.feed.application.port.out.VotedFeedCacheRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedVoteCount;
import kr.co.deepple.feed.domain.PollFeed;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedBlockFixture;
import kr.co.deepple.feed.fixture.FeedVoteFixture;
import kr.co.deepple.feed.fixture.PollFeedFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@DisplayName("피드 투표 통합 테스트")
class VoteFeedServiceIntegrationTest extends IntegrationTestSupport {

  private static final long VIEWER_MEMBER_ID = 1L;
  private static final long WRITER_MEMBER_ID = 2L;
  private static final int CONCURRENT_REQUESTS = 10;

  @Autowired private VoteFeedService voteFeedService;

  @Autowired private GetFeedService getFeedService;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedBlockRepository feedBlockRepository;

  @Autowired private FeedVoteRepository feedVoteRepository;

  @Autowired private FeedVoteCountRepository feedVoteCountRepository;

  @Autowired private FlushFeedVoteCountsService flushFeedVoteCountsService;

  @Autowired private StringRedisTemplate redisTemplate;

  @Autowired private VotedFeedCacheRepository votedFeedCacheRepository;

  private FeedAuthor writer;

  @BeforeEach
  void setUp() {
    feedAuthorRepository.save(activeMember(VIEWER_MEMBER_ID, "GOLD"));
    writer = feedAuthorRepository.save(activeMember(WRITER_MEMBER_ID, "GOLD"));
  }

  @Test
  @DisplayName("투표하면 기록이 생기고 응답에 내 선택과 결과가 들어 있다")
  void vote() {
    // given
    PollFeed feed = savePollFeed(writer, "GOLD");
    long yes = optionId(feed, 0);
    long no = optionId(feed, 1);
    feedVoteRepository.save(
        FeedVoteFixture.builder().feedId(feed.getId()).memberId(10L).optionId(no).build());
    feedVoteRepository.save(
        FeedVoteFixture.builder().feedId(feed.getId()).memberId(11L).optionId(no).build());
    feedVoteRepository.save(
        FeedVoteFixture.builder().feedId(feed.getId()).memberId(12L).optionId(no).build());

    // when
    FeedPollView poll = voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), yes);

    // then
    assertThat(poll.myOptionId()).isEqualTo(yes);
    assertThat(poll.isResultVisible()).isTrue();
    assertThat(poll.participantCount()).isEqualTo(4L);
    assertThat(poll.options()).extracting(FeedPollOptionView::votePercent).containsExactly(25, 75);
    assertThat(feedVoteRepository.count()).isEqualTo(4L);
  }

  @Test
  @DisplayName("투표한 뒤 상세 조회에도 내 선택과 결과가 보인다")
  void showResultAfterVote() {
    // given
    PollFeed feed = savePollFeed(writer, "GOLD");
    long yes = optionId(feed, 0);
    getFeedService.getFeed(VIEWER_MEMBER_ID, feed.getId());

    // when
    voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), yes);

    // then
    FeedPollView poll = getFeedService.getFeed(VIEWER_MEMBER_ID, feed.getId()).poll();
    assertThat(poll.myOptionId()).isEqualTo(yes);
    assertThat(poll.participantCount()).isEqualTo(1L);
    assertThat(poll.options()).extracting(FeedPollOptionView::votePercent).containsExactly(100, 0);
  }

  @Test
  @DisplayName("투표 기록을 DB에서 읽어 캐시에 채우는 사이에 투표해도 내 선택이 보인다")
  void showMyOptionWhenVotedWhileLoadingVotedFeeds() {
    // given
    PollFeed feed = savePollFeed(writer, "GOLD");
    long yes = optionId(feed, 0);
    Map<Long, Long> loadingVotedOptionIds =
        feedVoteRepository.findVotedOptionsByMemberId(VIEWER_MEMBER_ID).stream()
            .collect(Collectors.toMap(VotedOption::feedId, VotedOption::optionId));

    // when
    voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), yes);
    votedFeedCacheRepository.saveIfAbsent(VIEWER_MEMBER_ID, loadingVotedOptionIds);

    // then
    FeedPollView poll = getFeedService.getFeed(VIEWER_MEMBER_ID, feed.getId()).poll();
    assertThat(poll.myOptionId()).isEqualTo(yes);
    assertThat(poll.isResultVisible()).isTrue();
  }

  @Test
  @DisplayName("이미 투표한 피드에 다시 투표하면 거절하고 표 수는 그대로다")
  void rejectDuplicateVote() {
    // given
    PollFeed feed = savePollFeed(writer, "GOLD");
    voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), optionId(feed, 0));

    // when & then
    assertThatThrownBy(
            () -> voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), optionId(feed, 1)))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_VOTE_ALREADY_EXISTS);
    FeedPollView poll = getFeedService.getFeed(VIEWER_MEMBER_ID, feed.getId()).poll();
    assertThat(poll.myOptionId()).isEqualTo(optionId(feed, 0));
    assertThat(poll.participantCount()).isEqualTo(1L);
    assertThat(feedVoteRepository.count()).isEqualTo(1L);
  }

  @Test
  @DisplayName("같은 회원의 투표가 동시에 와도 하나만 저장되고 나머지는 이미 투표함으로 거절한다")
  void voteConcurrentlyBySameMember() throws Exception {
    // given
    PollFeed feed = savePollFeed(writer, "GOLD");
    long yes = optionId(feed, 0);
    getFeedService.getFeed(VIEWER_MEMBER_ID, feed.getId());

    // when
    List<Throwable> failures =
        runConcurrently(index -> voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), yes));

    // then
    assertThat(failures).hasSize(CONCURRENT_REQUESTS - 1);
    assertThat(failures)
        .allSatisfy(failure ->
            assertThat(failure)
                .isInstanceOf(DeeppleException.class)
                .extracting("errorCode")
                .isEqualTo(FeedErrorCode.FEED_VOTE_ALREADY_EXISTS));
    assertThat(feedVoteRepository.count()).isEqualTo(1L);
    assertThat(getFeedService.getFeed(VIEWER_MEMBER_ID, feed.getId()).poll().participantCount())
        .isEqualTo(1L);
  }

  @Test
  @DisplayName("여러 회원이 동시에 투표해도 표 수가 모두 반영된다")
  void voteConcurrentlyByManyMembers() throws Exception {
    // given
    PollFeed feed = savePollFeed(writer, "GOLD");
    long yes = optionId(feed, 0);
    for (long memberId = 100L; memberId < 100L + CONCURRENT_REQUESTS; memberId++) {
      feedAuthorRepository.save(activeMember(memberId, "GOLD"));
    }
    getFeedService.getFeed(WRITER_MEMBER_ID, feed.getId());

    // when
    List<Throwable> failures =
        runConcurrently(index -> voteFeedService.vote(100L + index, feed.getId(), yes));

    // then
    assertThat(failures).isEmpty();
    assertThat(feedVoteRepository.count()).isEqualTo(CONCURRENT_REQUESTS);
    assertThat(voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), yes).participantCount())
        .isEqualTo(CONCURRENT_REQUESTS + 1L);
  }

  @Test
  @DisplayName("카운터가 없을 때 여러 회원이 동시에 투표해도 표 수가 한 번씩만 반영된다")
  void voteConcurrentlyByManyMembersWithoutCounter() throws Exception {
    // given
    PollFeed feed = savePollFeed(writer, "GOLD");
    long yes = optionId(feed, 0);
    for (long memberId = 100L; memberId < 100L + CONCURRENT_REQUESTS; memberId++) {
      feedAuthorRepository.save(activeMember(memberId, "GOLD"));
    }
    getFeedService.getFeed(WRITER_MEMBER_ID, feed.getId());
    redisTemplate.delete("feed:vote-count:v1:" + feed.getId());

    // when
    List<Throwable> failures =
        runConcurrently(index -> voteFeedService.vote(100L + index, feed.getId(), yes));

    // then
    assertThat(failures).isEmpty();
    assertThat(feedVoteRepository.count()).isEqualTo(CONCURRENT_REQUESTS);
    assertThat(voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), yes).participantCount())
        .isEqualTo(CONCURRENT_REQUESTS + 1L);
  }

  @Test
  @DisplayName("작성자는 자기 투표 피드에 투표할 수 없다")
  void rejectVoteOnOwnFeed() {
    // given
    PollFeed feed = savePollFeed(writer, "GOLD");

    // when & then
    assertThatThrownBy(
            () -> voteFeedService.vote(WRITER_MEMBER_ID, feed.getId(), optionId(feed, 0)))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.SELF_FEED_VOTE);
    assertThat(feedVoteRepository.count()).isZero();
  }

  @Test
  @DisplayName("투표 피드가 아니거나 그 피드의 선택지가 아니면 거절한다")
  void rejectInvalidOption() {
    // given
    PollFeed feed = savePollFeed(writer, "GOLD");
    PollFeed otherFeed = savePollFeed(writer, "GOLD");
    long textFeedId =
        feedRepository
            .save(TextFeedFixture.builder().authorId(writer.getId()).grade("GOLD").build())
            .getId();

    // when & then
    assertThatThrownBy(
            () -> voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), optionId(otherFeed, 0)))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.POLL_OPTION_NOT_FOUND);
    assertThatThrownBy(() -> voteFeedService.vote(VIEWER_MEMBER_ID, textFeedId, optionId(feed, 0)))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.POLL_OPTION_NOT_FOUND);
    assertThat(feedVoteRepository.count()).isZero();
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
        FeedBlockFixture.builder().blockerId(VIEWER_MEMBER_ID).blockedId(4L).build());
    PollFeed deleted = savePollFeed(writer, "GOLD");
    deleted.delete();
    feedRepository.save(deleted);
    List<PollFeed> invisibleFeeds =
        List.of(
            savePollFeed(writer, "SILVER"),
            deleted,
            savePollFeed(dormant, "GOLD"),
            savePollFeed(blocked, "GOLD"));

    // when & then
    for (PollFeed feed : invisibleFeeds) {
      assertThatThrownBy(
              () -> voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), optionId(feed, 0)))
          .isInstanceOf(DeeppleException.class)
          .extracting("errorCode")
          .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    }
    assertThat(feedVoteRepository.count()).isZero();
  }

  @Test
  @DisplayName("카운터가 없을 때 투표하면 투표 기록을 세서 카운터와 feed_vote_counts를 채운다")
  void fillCounterFromVotesOnVote() {
    // given
    PollFeed feed = savePollFeed(writer, "GOLD");
    long yes = optionId(feed, 0);
    long no = optionId(feed, 1);
    feedVoteRepository.save(
        FeedVoteFixture.builder().feedId(feed.getId()).memberId(10L).optionId(yes).build());
    feedVoteRepository.save(
        FeedVoteFixture.builder().feedId(feed.getId()).memberId(11L).optionId(no).build());

    // when
    FeedPollView poll = voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), yes);
    flushFeedVoteCountsService.flush();

    // then
    assertThat(poll.participantCount()).isEqualTo(3L);
    assertThat(findVoteCounts()).containsOnly(Map.entry(yes, 2L), Map.entry(no, 1L));
  }

  @Test
  @DisplayName("투표로 카운터가 바뀌면 만료 시간을 다시 7일로 늘린다")
  void extendCounterTtlOnVote() {
    // given
    PollFeed feed = savePollFeed(writer, "GOLD");
    feedAuthorRepository.save(activeMember(3L, "GOLD"));
    voteFeedService.vote(3L, feed.getId(), optionId(feed, 0));
    String key = "feed:vote-count:v1:" + feed.getId();
    redisTemplate.expire(key, Duration.ofSeconds(10));

    // when
    voteFeedService.vote(VIEWER_MEMBER_ID, feed.getId(), optionId(feed, 1));

    // then
    assertThat(redisTemplate.getExpire(key)).isGreaterThan(Duration.ofDays(6).toSeconds());
  }

  private List<Throwable> runConcurrently(IndexedTask task) throws Exception {
    CountDownLatch start = new CountDownLatch(1);
    List<Throwable> failures = new ArrayList<>();
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
        try {
          future.get();
        } catch (ExecutionException e) {
          failures.add(e.getCause());
        }
      }
    }
    return failures;
  }

  private PollFeed savePollFeed(FeedAuthor author, String grade) {
    return feedRepository.save(
        PollFeedFixture.builder().authorId(author.getId()).grade(grade).build());
  }

  private long optionId(PollFeed feed, int index) {
    return feed.getOptions().get(index).getId();
  }

  private Map<Long, Long> findVoteCounts() {
    return feedVoteCountRepository.findAll().stream()
        .collect(Collectors.toMap(FeedVoteCount::getOptionId, FeedVoteCount::getVoteCount));
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
