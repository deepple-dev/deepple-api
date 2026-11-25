package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedCommentFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("댓글 삭제 통합 테스트")
class DeleteFeedCommentServiceIntegrationTest extends IntegrationTestSupport {

  private static final long MY_MEMBER_ID = 1L;
  private static final long FEED_WRITER_MEMBER_ID = 2L;

  @Autowired private DeleteFeedCommentService deleteFeedCommentService;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedCommentRepository feedCommentRepository;

  private FeedAuthor me;

  @BeforeEach
  void setUp() {
    me = feedAuthorRepository.save(FeedAuthorFixture.builder().memberId(MY_MEMBER_ID).build());
    feedAuthorRepository.save(FeedAuthorFixture.builder().memberId(FEED_WRITER_MEMBER_ID).build());
  }

  @Test
  @DisplayName("내 댓글을 지우면 행은 남기고 삭제 시각을 기록한다")
  void deleteOwnComment() {
    // given
    long commentId = saveComment(me);

    // when
    deleteFeedCommentService.deleteComment(MY_MEMBER_ID, commentId);

    // then
    assertThat(feedCommentRepository.findById(commentId).orElseThrow().isDeleted()).isTrue();
  }

  @Test
  @DisplayName("이미 지운 댓글을 다시 지워도 성공한다")
  void deleteTwice() {
    // given
    long commentId = saveComment(me);
    deleteFeedCommentService.deleteComment(MY_MEMBER_ID, commentId);

    // when & then
    assertThatCode(() -> deleteFeedCommentService.deleteComment(MY_MEMBER_ID, commentId))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("피드 작성자라도 다른 사람의 댓글은 없는 댓글로 거절한다")
  void rejectDeletingOthersComment() {
    // given
    long commentId = saveComment(me);

    // when & then
    assertThatThrownBy(
            () -> deleteFeedCommentService.deleteComment(FEED_WRITER_MEMBER_ID, commentId))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_COMMENT_NOT_FOUND);
    assertThat(feedCommentRepository.findById(commentId).orElseThrow().isDeleted()).isFalse();
  }

  private long saveComment(FeedAuthor author) {
    return feedCommentRepository
        .save(FeedCommentFixture.builder().authorId(author.getId()).build())
        .getId();
  }
}
