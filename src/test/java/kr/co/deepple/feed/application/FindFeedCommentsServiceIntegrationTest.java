package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import kr.co.deepple.feed.domain.FeedCommentStatus;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedBlockFixture;
import kr.co.deepple.feed.fixture.FeedCommentFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("댓글 목록 조회 통합 테스트")
class FindFeedCommentsServiceIntegrationTest extends IntegrationTestSupport {

  private static final long MY_MEMBER_ID = 1L;
  private static final long WRITER_MEMBER_ID = 2L;
  private static final long BLOCKED_BY_ME_MEMBER_ID = 3L;
  private static final long BLOCKING_ME_MEMBER_ID = 4L;

  @Autowired private FindFeedCommentsService findFeedCommentsService;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedBlockRepository feedBlockRepository;

  @Autowired private FeedCommentRepository feedCommentRepository;

  private FeedAuthor me;
  private FeedAuthor writer;
  private FeedAuthor blockedByMe;
  private FeedAuthor blockingMe;
  private long feedId;

  @BeforeEach
  void setUp() {
    me = feedAuthorRepository.save(activeMember(MY_MEMBER_ID));
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
  }

  @Test
  @DisplayName("최상위 댓글을 최신순으로 20개씩 주고 커서로 다음 페이지를 준다")
  void findLatestCommentsWithCursor() {
    // given
    List<Long> commentIds = new ArrayList<>();
    for (int i = 0; i < 25; i++) {
      commentIds.add(saveComment(writer).getId());
    }
    List<Long> latestFirst = commentIds.reversed();

    // when
    FeedCommentViews firstPage = findFeedCommentsService.findComments(MY_MEMBER_ID, feedId, null);
    FeedCommentViews secondPage =
        findFeedCommentsService.findComments(MY_MEMBER_ID, feedId, firstPage.nextCursor());

    // then
    assertThat(commentIds(firstPage)).isEqualTo(latestFirst.subList(0, 20));
    assertThat(firstPage.hasMore()).isTrue();
    assertThat(firstPage.nextCursor()).isEqualTo(String.valueOf(latestFirst.get(19)));
    assertThat(commentIds(secondPage)).isEqualTo(latestFirst.subList(20, 25));
    assertThat(secondPage.hasMore()).isFalse();
    assertThat(secondPage.nextCursor()).isNull();
  }

  @Test
  @DisplayName("차단 관계(양방향)인 회원의 댓글은 쿼리에서 걸러 20개를 채운다")
  void excludeBlockedCommentsAndFillPage() {
    // given
    List<Long> visibleIds = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      if (i % 7 == 0) {
        saveComment(blockedByMe);
        saveComment(blockingMe);
      }
      visibleIds.add(saveComment(writer).getId());
    }

    // when
    FeedCommentViews page = findFeedCommentsService.findComments(MY_MEMBER_ID, feedId, null);

    // then
    assertThat(commentIds(page)).isEqualTo(visibleIds.reversed());
    assertThat(page.hasMore()).isFalse();
  }

  @Test
  @DisplayName("차단 관계가 있어도 회원 ID가 없는 공식 계정의 댓글과 답글은 정상으로 보인다")
  void keepOfficialCommentsWithBlocks() {
    // given
    FeedAuthor official = feedAuthorRepository.save(FeedAuthorFixture.official());
    FeedComment comment = saveComment(official);
    saveReply(comment, official);

    // when
    FeedCommentViews page = findFeedCommentsService.findComments(MY_MEMBER_ID, feedId, null);

    // then
    assertThat(commentIds(page)).containsExactly(comment.getId());
    assertThat(page.comments().getFirst().displayStatus()).isEqualTo("VISIBLE");
    assertThat(page.comments().getFirst().replyCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("답글은 목록에 넣지 않고, 차단 관계인 회원의 답글을 뺀 답글 수를 붙인다")
  void countRepliesExceptBlocked() {
    // given
    FeedComment withReplies = saveComment(writer);
    FeedComment withoutReplies = saveComment(writer);
    saveReply(withReplies, writer);
    feedCommentRepository.save(
        FeedCommentFixture.builder()
            .feedId(feedId)
            .parentId(withReplies.getId())
            .authorId(me.getId())
            .deletedAt(LocalDateTime.now())
            .build());
    saveReply(withReplies, blockedByMe);
    saveReply(withReplies, blockingMe);

    // when
    FeedCommentViews page = findFeedCommentsService.findComments(MY_MEMBER_ID, feedId, null);

    // then
    Map<Long, Integer> replyCounts =
        page.comments().stream()
            .collect(Collectors.toMap(FeedCommentView::commentId, FeedCommentView::replyCount));
    assertThat(replyCounts)
        .containsOnly(Map.entry(withReplies.getId(), 2), Map.entry(withoutReplies.getId(), 0));
  }

  @Test
  @DisplayName("삭제·숨김·비활성화 작성자 댓글은 대체 상태로 주고 작성자와 내용을 비운다")
  void decideDisplayStatus() {
    // given
    FeedAuthor dormant =
        feedAuthorRepository.save(
            FeedAuthorFixture.builder()
                .memberId(5L)
                .activityStatus("DORMANT")
                .grade("GOLD")
                .build());
    FeedComment visible = saveComment(writer);
    FeedComment deleted =
        feedCommentRepository.save(
            FeedCommentFixture.builder()
                .feedId(feedId)
                .authorId(me.getId())
                .status(FeedCommentStatus.HIDDEN_BY_REPORT)
                .deletedAt(LocalDateTime.now())
                .build());
    FeedComment hidden =
        feedCommentRepository.save(
            FeedCommentFixture.builder()
                .feedId(feedId)
                .authorId(dormant.getId())
                .status(FeedCommentStatus.HIDDEN_BY_REPORT)
                .build());
    FeedComment inactiveAuthor = saveComment(dormant);

    // when
    Map<Long, FeedCommentView> views =
        findFeedCommentsService.findComments(MY_MEMBER_ID, feedId, null).comments().stream()
            .collect(Collectors.toMap(FeedCommentView::commentId, view -> view));

    // then
    assertThat(views.get(visible.getId()).displayStatus()).isEqualTo("VISIBLE");
    assertThat(views.get(visible.getId()).author().memberId()).isEqualTo(WRITER_MEMBER_ID);
    assertThat(views.get(visible.getId()).content()).isEqualTo(visible.getContent());
    assertThat(views.get(deleted.getId()).displayStatus()).isEqualTo("DELETED");
    assertThat(views.get(deleted.getId()).isMine()).isTrue();
    assertThat(views.get(hidden.getId()).displayStatus()).isEqualTo("HIDDEN");
    assertThat(views.get(inactiveAuthor.getId()).displayStatus()).isEqualTo("INACTIVE_AUTHOR");
    List.of(deleted, hidden, inactiveAuthor)
        .forEach(comment -> {
          assertThat(views.get(comment.getId()).author()).isNull();
          assertThat(views.get(comment.getId()).content()).isNull();
        });
  }

  @Test
  @DisplayName("내가 쓴 댓글만 isMine이 true다")
  void markMyComments() {
    // given
    FeedComment mine = saveComment(me);
    FeedComment others = saveComment(writer);

    // when
    Map<Long, Boolean> isMine =
        findFeedCommentsService.findComments(MY_MEMBER_ID, feedId, null).comments().stream()
            .collect(Collectors.toMap(FeedCommentView::commentId, FeedCommentView::isMine));

    // then
    assertThat(isMine)
        .containsOnly(Map.entry(mine.getId(), true), Map.entry(others.getId(), false));
  }

  @Test
  @DisplayName("볼 수 없는 피드의 댓글은 없는 피드로 거절한다")
  void rejectInvisibleFeed() {
    // given
    long blockedFeedId =
        feedRepository
            .save(TextFeedFixture.builder().authorId(blockingMe.getId()).grade("GOLD").build())
            .getId();

    // when & then
    assertThatThrownBy(
            () -> findFeedCommentsService.findComments(MY_MEMBER_ID, blockedFeedId, null))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
  }

  private FeedComment saveComment(FeedAuthor author) {
    return feedCommentRepository.save(
        FeedCommentFixture.builder().feedId(feedId).authorId(author.getId()).build());
  }

  private void saveReply(FeedComment parent, FeedAuthor author) {
    feedCommentRepository.save(
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
