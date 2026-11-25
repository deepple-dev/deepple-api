package kr.co.deepple.feed.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.fixture.FeedCommentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("댓글 규칙 테스트")
class FeedCommentTest {

  @Test
  @DisplayName("댓글은 공개 상태로 만들어지고 부모가 없다")
  void createTopLevelComment() {
    FeedComment comment = FeedComment.create(5L, 1L, "저도 가고 싶어요");

    assertThat(comment.getFeedId()).isEqualTo(5L);
    assertThat(comment.isReply()).isFalse();
    assertThat(comment.getStatus()).isEqualTo(FeedCommentStatus.PUBLISHED);
    assertThat(comment.isWrittenBy(1L)).isTrue();
    assertThat(comment.isWrittenBy(2L)).isFalse();
  }

  @Test
  @DisplayName("답글은 부모 댓글의 피드에 부모 댓글 ID로 연결된다")
  void createReply() {
    FeedComment parent = FeedCommentFixture.builder().id(10L).feedId(5L).build();

    FeedComment reply = FeedComment.reply(parent, 2L, "같이 가요");

    assertThat(reply.getFeedId()).isEqualTo(5L);
    assertThat(reply.getParentId()).isEqualTo(10L);
    assertThat(reply.isReply()).isTrue();
  }

  @Test
  @DisplayName("답글에는 답글을 달 수 없다")
  void rejectReplyToReply() {
    FeedComment reply = FeedCommentFixture.builder().id(20L).feedId(5L).parentId(10L).build();

    assertThatThrownBy(() -> FeedComment.reply(reply, 2L, "답글의 답글"))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_PARENT_COMMENT);
  }

  @Test
  @DisplayName("내용은 줄바꿈과 이모지를 포함해 UTF-16 기준 100자까지 쓸 수 있다")
  void acceptContentUpTo100Characters() {
    String content = "가".repeat(97) + "\n" + "🙂";

    assertThat(FeedComment.create(1L, 1L, content).getContent()).hasSize(100);
  }

  @ParameterizedTest
  @ValueSource(strings = {" ", "\n"})
  @DisplayName("빈 내용은 거절한다")
  void rejectBlankContent(String content) {
    assertInvalidContent(content);
  }

  @Test
  @DisplayName("UTF-16 기준 100자를 넘으면 거절한다")
  void rejectContentOver100Characters() {
    assertInvalidContent("가".repeat(99) + "🙂");
  }

  @Test
  @DisplayName("신고로 숨기면 숨김_신고가 되고, 다시 숨겨도 그대로다")
  void hideByReport() {
    FeedComment comment = FeedComment.create(1L, 1L, "댓글");

    comment.hideByReport();
    comment.hideByReport();

    assertThat(comment.isHiddenByReport()).isTrue();
  }

  private void assertInvalidContent(String content) {
    assertThatThrownBy(() -> FeedComment.create(1L, 1L, content))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_COMMENT_CONTENT);
  }
}
