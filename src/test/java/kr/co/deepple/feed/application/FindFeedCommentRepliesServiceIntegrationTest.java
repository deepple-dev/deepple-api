package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedCommentView;
import kr.co.deepple.feed.application.dto.FeedCommentViews;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedComment;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedBlockFixture;
import kr.co.deepple.feed.fixture.FeedCommentFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("답글 목록 조회 통합 테스트")
class FindFeedCommentRepliesServiceIntegrationTest extends IntegrationTestSupport {

  private static final long MY_MEMBER_ID = 1L;
  private static final long WRITER_MEMBER_ID = 2L;
  private static final long BLOCKED_BY_ME_MEMBER_ID = 3L;
  private static final long BLOCKING_ME_MEMBER_ID = 4L;

  @Autowired private FindFeedCommentRepliesService findFeedCommentRepliesService;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedBlockRepository feedBlockRepository;

  @Autowired private FeedCommentRepository feedCommentRepository;

  private FeedAuthor writer;
  private FeedAuthor blockedByMe;
  private FeedAuthor blockingMe;
  private long feedId;
  private FeedComment parent;

  @BeforeEach
  void setUp() {
    feedAuthorRepository.save(activeMember(MY_MEMBER_ID));
    writer = feedAuthorRepository.save(activeMember(WRITER_MEMBER_ID));
    blockedByMe = feedAuthorRepository.save(activeMember(BLOCKED_BY_ME_MEMBER_ID));
    blockingMe = feedAuthorRepository.save(activeMember(BLOCKING_ME_MEMBER_ID));
    feedBlockRepository.save(
        FeedBlockFixture.builder()
            .blockerId(MY_MEMBER_ID)
            .blockedId(BLOCKED_BY_ME_MEMBER_ID)
            .build());
    feedBlockRepository.save(
        FeedBlockFixture.builder()
            .blockerId(BLOCKING_ME_MEMBER_ID)
            .blockedId(MY_MEMBER_ID)
            .build());
    feedId =
        feedRepository
            .save(TextFeedFixture.builder().authorId(writer.getId()).grade("GOLD").build())
            .getId();
    parent =
        feedCommentRepository.save(
            FeedCommentFixture.builder().feedId(feedId).authorId(writer.getId()).build());
  }

  @Test
  @DisplayName("답글을 최신순으로 20개씩 주고 커서로 다음 페이지를 준다")
  void findLatestRepliesWithCursor() {
    // given
    List<Long> replyIds = new ArrayList<>();
    for (int i = 0; i < 22; i++) {
      replyIds.add(saveReply(parent, writer).getId());
    }
    List<Long> latestFirst = replyIds.reversed();

    // when
    FeedCommentViews firstPage =
        findFeedCommentRepliesService.findReplies(MY_MEMBER_ID, parent.getId(), null);
    FeedCommentViews secondPage =
        findFeedCommentRepliesService.findReplies(
            MY_MEMBER_ID, parent.getId(), firstPage.nextCursor());

    // then
    assertThat(commentIds(firstPage)).isEqualTo(latestFirst.subList(0, 20));
    assertThat(firstPage.hasMore()).isTrue();
    assertThat(commentIds(secondPage)).isEqualTo(latestFirst.subList(20, 22));
    assertThat(secondPage.hasMore()).isFalse();
    assertThat(secondPage.comments())
        .allSatisfy(reply -> {
          assertThat(reply.parentCommentId()).isEqualTo(parent.getId());
          assertThat(reply.replyCount()).isZero();
        });
  }

  @Test
  @DisplayName("차단 관계(양방향)인 회원의 답글은 빼고 삭제된 답글은 남긴다")
  void excludeBlockedReplies() {
    // given
    FeedComment visible = saveReply(parent, writer);
    FeedComment deleted =
        feedCommentRepository.save(
            FeedCommentFixture.builder()
                .feedId(feedId)
                .parentId(parent.getId())
                .authorId(writer.getId())
                .deletedAt(LocalDateTime.now())
                .build());
    saveReply(parent, blockedByMe);
    saveReply(parent, blockingMe);

    // when
    FeedCommentViews replies =
        findFeedCommentRepliesService.findReplies(MY_MEMBER_ID, parent.getId(), null);

    // then
    assertThat(commentIds(replies)).containsExactly(deleted.getId(), visible.getId());
  }

  @Test
  @DisplayName("삭제된 댓글의 답글도 볼 수 있다")
  void findRepliesOfDeletedComment() {
    // given
    FeedComment reply = saveReply(parent, writer);
    parent.delete();
    feedCommentRepository.save(parent);

    // when
    FeedCommentViews replies =
        findFeedCommentRepliesService.findReplies(MY_MEMBER_ID, parent.getId(), null);

    // then
    assertThat(commentIds(replies)).containsExactly(reply.getId());
  }

  @Test
  @DisplayName("답글의 답글을 조회하면 빈 목록이다")
  void findRepliesOfReply() {
    // given
    FeedComment reply = saveReply(parent, writer);

    // when
    FeedCommentViews replies =
        findFeedCommentRepliesService.findReplies(MY_MEMBER_ID, reply.getId(), null);

    // then
    assertThat(replies.comments()).isEmpty();
    assertThat(replies.hasMore()).isFalse();
  }

  @Test
  @DisplayName("없는 댓글이나 차단 관계인 회원의 댓글은 없는 댓글로 거절한다")
  void rejectMissingOrBlockedParent() {
    // given
    FeedComment blockedParent =
        feedCommentRepository.save(
            FeedCommentFixture.builder().feedId(feedId).authorId(blockingMe.getId()).build());

    // when & then
    for (long commentId : List.of(blockedParent.getId(), 999L)) {
      assertThatThrownBy(
              () -> findFeedCommentRepliesService.findReplies(MY_MEMBER_ID, commentId, null))
          .isInstanceOf(DeeppleException.class)
          .extracting("errorCode")
          .isEqualTo(FeedErrorCode.FEED_COMMENT_NOT_FOUND);
    }
  }

  @Test
  @DisplayName("볼 수 없는 피드의 답글은 없는 피드로 거절한다")
  void rejectInvisibleFeed() {
    // given
    long silverFeedId =
        feedRepository
            .save(TextFeedFixture.builder().authorId(writer.getId()).grade("SILVER").build())
            .getId();
    FeedComment silverComment =
        feedCommentRepository.save(
            FeedCommentFixture.builder().feedId(silverFeedId).authorId(writer.getId()).build());

    // when & then
    assertThatThrownBy(() ->
            findFeedCommentRepliesService.findReplies(MY_MEMBER_ID, silverComment.getId(), null))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
  }

  private FeedComment saveReply(FeedComment parent, FeedAuthor author) {
    return feedCommentRepository.save(
        FeedCommentFixture.builder()
            .feedId(feedId)
            .parentId(parent.getId())
            .authorId(author.getId())
            .build());
  }

  private static List<Long> commentIds(FeedCommentViews views) {
    return views.comments().stream().map(FeedCommentView::commentId).toList();
  }

  private static FeedAuthor activeMember(long memberId) {
    return FeedAuthorFixture.builder()
        .memberId(memberId)
        .activityStatus("ACTIVE")
        .grade("GOLD")
        .build();
  }
}
