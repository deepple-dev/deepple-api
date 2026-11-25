package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedCommentCreateRequest;
import kr.co.deepple.feed.application.dto.FeedCommentView;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedAuthorType;
import kr.co.deepple.feed.domain.FeedComment;
import kr.co.deepple.feed.domain.FeedCommentStatus;
import kr.co.deepple.feed.domain.FeedCount;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedBlockFixture;
import kr.co.deepple.feed.fixture.FeedCommentFixture;
import kr.co.deepple.feed.fixture.OfficialFeedFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("댓글 작성 통합 테스트")
class CreateFeedCommentServiceIntegrationTest extends IntegrationTestSupport {

  private static final long MY_MEMBER_ID = 1L;
  private static final long WRITER_MEMBER_ID = 2L;
  private static final int CONCURRENT_REQUESTS = 10;

  @Autowired private CreateFeedCommentService createFeedCommentService;

  @Autowired private GetFeedService getFeedService;

  @Autowired private FlushFeedCommentCountsService flushFeedCommentCountsService;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedBlockRepository feedBlockRepository;

  @Autowired private FeedCommentRepository feedCommentRepository;

  @Autowired private FeedCountRepository feedCountRepository;

  @Autowired private StringRedisTemplate redisTemplate;

  private FeedAuthor me;
  private FeedAuthor writer;
  private long feedId;

  @BeforeEach
  void setUp() {
    me = feedAuthorRepository.save(activeMember(MY_MEMBER_ID, "GOLD"));
    writer = feedAuthorRepository.save(activeMember(WRITER_MEMBER_ID, "GOLD"));
    feedId = saveFeed(writer, "GOLD");
  }

  @Test
  @DisplayName("댓글을 쓰면 내가 쓴 정상 댓글로 응답하고 피드의 댓글 수가 오른다")
  void createComment() {
    // when
    FeedCommentView view =
        createFeedCommentService.createComment(MY_MEMBER_ID, feedId, request("저도 가고 싶어요", null));

    // then
    assertThat(view.parentCommentId()).isNull();
    assertThat(view.displayStatus()).isEqualTo("VISIBLE");
    assertThat(view.author().memberId()).isEqualTo(MY_MEMBER_ID);
    assertThat(view.content()).isEqualTo("저도 가고 싶어요");
    assertThat(view.replyCount()).isZero();
    assertThat(view.isMine()).isTrue();
    assertThat(feedCommentRepository.count()).isEqualTo(1L);
    assertThat(getFeedService.getFeed(MY_MEMBER_ID, feedId).commentCount()).isEqualTo(1L);
  }

  @Test
  @DisplayName("부모 댓글 ID를 보내면 그 댓글의 답글이 된다")
  void createReply() {
    // given
    FeedComment parent = saveComment(writer);

    // when
    FeedCommentView view =
        createFeedCommentService.createComment(
            MY_MEMBER_ID, feedId, request("같이 가요", parent.getId()));

    // then
    assertThat(view.parentCommentId()).isEqualTo(parent.getId());
    assertThat(getFeedService.getFeed(MY_MEMBER_ID, feedId).commentCount()).isEqualTo(2L);
  }

  @Test
  @DisplayName("답글에 답글을 달면 거절한다")
  void rejectReplyToReply() {
    // given
    FeedComment parent = saveComment(writer);
    FeedComment reply =
        feedCommentRepository.save(
            FeedCommentFixture.builder()
                .feedId(feedId)
                .parentId(parent.getId())
                .authorId(writer.getId())
                .build());

    // when & then
    assertThatThrownBy(() ->
            createFeedCommentService.createComment(
                MY_MEMBER_ID, feedId, request("답글의 답글", reply.getId())))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_PARENT_COMMENT);
  }

  @Test
  @DisplayName("정상으로 보이지 않는 댓글이나 다른 피드의 댓글에는 답글을 달 수 없다")
  void rejectUnreplyableParents() {
    // given
    FeedAuthor dormant = feedAuthorRepository.save(member(3L, "DORMANT"));
    FeedAuthor blockedByMe = feedAuthorRepository.save(activeMember(4L, "GOLD"));
    FeedAuthor blockingMe = feedAuthorRepository.save(activeMember(5L, "GOLD"));
    feedBlockRepository.save(
        FeedBlockFixture.builder().blockerId(MY_MEMBER_ID).blockedId(4L).build());
    feedBlockRepository.save(
        FeedBlockFixture.builder().blockerId(5L).blockedId(MY_MEMBER_ID).build());
    long otherFeedId = saveFeed(writer, "GOLD");
    List<Long> unreplyableIds =
        List.of(
            feedCommentRepository
                .save(
                    FeedCommentFixture.builder()
                        .feedId(otherFeedId)
                        .authorId(writer.getId())
                        .build())
                .getId(),
            feedCommentRepository
                .save(
                    FeedCommentFixture.builder()
                        .feedId(feedId)
                        .authorId(writer.getId())
                        .deletedAt(LocalDateTime.now())
                        .build())
                .getId(),
            feedCommentRepository
                .save(
                    FeedCommentFixture.builder()
                        .feedId(feedId)
                        .authorId(writer.getId())
                        .status(FeedCommentStatus.HIDDEN_BY_REPORT)
                        .build())
                .getId(),
            saveComment(dormant).getId(),
            saveComment(blockedByMe).getId(),
            saveComment(blockingMe).getId(),
            999L);
    long savedCount = feedCommentRepository.count();

    // when & then
    for (long parentCommentId : unreplyableIds) {
      assertThatThrownBy(() ->
              createFeedCommentService.createComment(
                  MY_MEMBER_ID, feedId, request("답글", parentCommentId)))
          .isInstanceOf(DeeppleException.class)
          .extracting("errorCode")
          .isEqualTo(FeedErrorCode.FEED_COMMENT_NOT_FOUND);
    }
    assertThat(feedCommentRepository.count()).isEqualTo(savedCount);
  }

  @Test
  @DisplayName("볼 수 없는 피드와 비활성화 회원의 피드에는 댓글을 달 수 없다")
  void rejectInvisibleFeeds() {
    // given
    FeedAuthor dormant = feedAuthorRepository.save(member(3L, "DORMANT"));
    FeedAuthor blocked = feedAuthorRepository.save(activeMember(4L, "GOLD"));
    feedBlockRepository.save(
        FeedBlockFixture.builder().blockerId(4L).blockedId(MY_MEMBER_ID).build());
    long deletedFeedId = saveFeed(writer, "GOLD");
    Feed deleted = feedRepository.findById(deletedFeedId).orElseThrow();
    deleted.delete();
    feedRepository.save(deleted);
    List<Long> invisibleFeedIds =
        List.of(
            saveFeed(writer, "SILVER"),
            saveFeed(dormant, "GOLD"),
            saveFeed(blocked, "GOLD"),
            deletedFeedId);

    // when & then
    for (long invisibleFeedId : invisibleFeedIds) {
      assertThatThrownBy(() ->
              createFeedCommentService.createComment(
                  MY_MEMBER_ID, invisibleFeedId, request("댓글", null)))
          .isInstanceOf(DeeppleException.class)
          .extracting("errorCode")
          .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    }
    assertThat(feedCommentRepository.count()).isZero();
  }

  @Test
  @DisplayName("관리자 피드와 내 피드에도 댓글을 달 수 있다")
  void commentOnOfficialAndMyFeed() {
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
    long myFeedId = saveFeed(me, "GOLD");

    // when
    createFeedCommentService.createComment(MY_MEMBER_ID, officialFeedId, request("공지 확인", null));
    createFeedCommentService.createComment(MY_MEMBER_ID, myFeedId, request("내 피드", null));

    // then
    assertThat(feedCommentRepository.count()).isEqualTo(2L);
  }

  @Test
  @DisplayName("카운터가 없을 때 댓글을 쓰면 삭제된 댓글까지 세서 카운터와 feed_counts를 채운다")
  void fillCounterFromCommentsOnCreate() {
    // given
    saveComment(writer);
    feedCommentRepository.save(
        FeedCommentFixture.builder()
            .feedId(feedId)
            .authorId(writer.getId())
            .deletedAt(LocalDateTime.now())
            .build());

    // when
    createFeedCommentService.createComment(MY_MEMBER_ID, feedId, request("세 번째", null));
    flushFeedCommentCountsService.flush();

    // then
    assertThat(getFeedService.getFeed(MY_MEMBER_ID, feedId).commentCount()).isEqualTo(3L);
    assertThat(feedCountRepository.findById(feedId))
        .get()
        .extracting(FeedCount::getCommentCount)
        .isEqualTo(3L);
  }

  @Test
  @DisplayName("카운터가 없을 때 여러 회원이 동시에 댓글을 써도 수가 한 번씩만 오른다")
  void createConcurrentlyByManyMembersWithoutCounter() throws Exception {
    // given
    for (long memberId = 100L; memberId < 100L + CONCURRENT_REQUESTS; memberId++) {
      feedAuthorRepository.save(activeMember(memberId, "GOLD"));
    }
    getFeedService.getFeed(MY_MEMBER_ID, feedId);
    redisTemplate.delete("feed:comment-count:v1:" + feedId);

    // when
    runConcurrently(
        index -> createFeedCommentService.createComment(100L + index, feedId, request("댓글", null)));

    // then
    assertThat(feedCommentRepository.count()).isEqualTo(CONCURRENT_REQUESTS);
    assertThat(getFeedService.getFeed(MY_MEMBER_ID, feedId).commentCount())
        .isEqualTo(CONCURRENT_REQUESTS);
  }

  @Test
  @DisplayName("댓글로 카운터가 바뀌면 만료 시간을 다시 7일로 늘린다")
  void extendCounterTtlOnCreate() {
    // given
    getFeedService.getFeed(MY_MEMBER_ID, feedId);
    String key = "feed:comment-count:v1:" + feedId;
    redisTemplate.expire(key, Duration.ofSeconds(10));

    // when
    createFeedCommentService.createComment(MY_MEMBER_ID, feedId, request("댓글", null));

    // then
    assertThat(redisTemplate.getExpire(key)).isGreaterThan(Duration.ofDays(6).toSeconds());
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

  private FeedComment saveComment(FeedAuthor author) {
    return feedCommentRepository.save(
        FeedCommentFixture.builder().feedId(feedId).authorId(author.getId()).build());
  }

  private long saveFeed(FeedAuthor author, String grade) {
    return feedRepository
        .save(TextFeedFixture.builder().authorId(author.getId()).grade(grade).build())
        .getId();
  }

  private static FeedCommentCreateRequest request(String content, Long parentCommentId) {
    return new FeedCommentCreateRequest(content, parentCommentId);
  }

  private static FeedAuthor activeMember(long memberId, String grade) {
    return FeedAuthorFixture.builder()
        .memberId(memberId)
        .activityStatus("ACTIVE")
        .grade(grade)
        .build();
  }

  private static FeedAuthor member(long memberId, String activityStatus) {
    return FeedAuthorFixture.builder()
        .memberId(memberId)
        .activityStatus(activityStatus)
        .grade("GOLD")
        .build();
  }

  @FunctionalInterface
  private interface IndexedTask {
    void run(int index);
  }
}
