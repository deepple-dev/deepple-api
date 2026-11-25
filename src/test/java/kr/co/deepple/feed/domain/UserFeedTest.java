package kr.co.deepple.feed.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("회원 피드 규칙 테스트")
class UserFeedTest {

  private static final TextPosition CENTER = new TextPosition(0.5, 0.5, 0.0, 1.0);
  private static final FeedImage IMAGE =
      FeedImage.of(1L, "images/members/1/1.webp", "images/members/1/1-thumbnail.webp", 0);
  private static final List<String> OPTIONS = List.of("짜장", "짬뽕");
  private static final String KEY = "8e03978e-40d5-43e8-bc93-6894a57f9324";

  @Test
  @DisplayName("배경과 사진 중 하나만 있어야 한다")
  void requireExactlyOneCanvas() {
    assertThat(TextFeed.create(1L, "GOLD", "배경", CENTER, 1L, null, KEY).getStatus())
        .isEqualTo(FeedStatus.PUBLISHED);
    assertThat(TextFeed.create(1L, "GOLD", "사진", CENTER, null, IMAGE, KEY).getStatus())
        .isEqualTo(FeedStatus.PENDING);

    assertErrorCode(
        () -> TextFeed.create(1L, "GOLD", "둘 다", CENTER, 1L, IMAGE, KEY),
        FeedErrorCode.INVALID_FEED_CANVAS);
    assertErrorCode(
        () -> PollFeed.create(1L, "GOLD", "둘 다 없음", CENTER, null, null, OPTIONS, KEY),
        FeedErrorCode.INVALID_FEED_CANVAS);
  }

  @Test
  @DisplayName("글 피드 내용은 줄바꿈을 포함해 50자까지 쓸 수 있다")
  void validateTextContent() {
    assertThat(
            TextFeed.create(1L, "GOLD", "가".repeat(49) + "\n", CENTER, 1L, null, KEY).getContent())
        .hasSize(50);

    assertErrorCode(
        () -> TextFeed.create(1L, "GOLD", "가".repeat(51), CENTER, 1L, null, KEY),
        FeedErrorCode.INVALID_FEED_CONTENT);
    assertErrorCode(
        () -> TextFeed.create(1L, "GOLD", " ", CENTER, 1L, null, KEY),
        FeedErrorCode.INVALID_FEED_CONTENT);
  }

  @ParameterizedTest
  @ValueSource(strings = {" ", "점심\n뭐 먹지", "가가가가가가가가가가가가가가가가가가가가가"})
  @DisplayName("투표 질문은 비었거나 줄바꿈이 있거나 20자를 넘으면 거절한다")
  void rejectInvalidQuestion(String question) {
    assertErrorCode(
        () -> PollFeed.create(1L, "GOLD", question, CENTER, 1L, null, OPTIONS, KEY),
        FeedErrorCode.INVALID_FEED_CONTENT);
  }

  @Test
  @DisplayName("투표 선택지는 2~4개이고 각각 줄바꿈 없이 20자까지 쓸 수 있다")
  void validateOptions() {
    PollFeed feed =
        PollFeed.create(
            1L, "GOLD", "점심 뭐 먹지", CENTER, 1L, null, List.of("가".repeat(20), "b", "c", "d"), KEY);
    assertThat(feed.getOptions()).hasSize(4);

    assertErrorCode(
        () -> PollFeed.create(1L, "GOLD", "점심", CENTER, 1L, null, List.of("짜장"), KEY),
        FeedErrorCode.INVALID_POLL_OPTIONS);
    assertErrorCode(
        () ->
            PollFeed.create(
                1L, "GOLD", "점심", CENTER, 1L, null, List.of("a", "b", "c", "d", "e"), KEY),
        FeedErrorCode.INVALID_POLL_OPTIONS);
    assertErrorCode(
        () -> PollFeed.create(1L, "GOLD", "점심", CENTER, 1L, null, List.of("짜장", "짬\n뽕"), KEY),
        FeedErrorCode.INVALID_POLL_OPTIONS);
    assertErrorCode(
        () ->
            PollFeed.create(1L, "GOLD", "점심", CENTER, 1L, null, List.of("짜장", "가".repeat(21)), KEY),
        FeedErrorCode.INVALID_POLL_OPTIONS);
  }

  private static void assertErrorCode(Runnable action, FeedErrorCode errorCode) {
    assertThatThrownBy(action::run)
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(errorCode);
  }

  @ParameterizedTest
  @EnumSource(
      value = FeedStatus.class,
      names = {"PUBLISHED", "REJECTED"})
  @DisplayName("심사 대기 피드를 공개하거나 반려할 수 있다")
  void reviewPendingFeed(FeedStatus status) {
    TextFeed feed = TextFeed.create(1L, "GOLD", "사진", CENTER, null, IMAGE, KEY);

    feed.changeStatus(status);

    assertThat(feed.getStatus()).isEqualTo(status);
    assertThat(feed.getPublishedAt() != null).isEqualTo(status == FeedStatus.PUBLISHED);
  }

  @Test
  @DisplayName("심사 결과는 다시 바꿀 수 있고, 다시 공개해도 처음 공개 시각을 유지한다")
  void changeReviewResult() {
    TextFeed feed = TextFeed.create(1L, "GOLD", "사진", CENTER, null, IMAGE, KEY);
    feed.changeStatus(FeedStatus.PUBLISHED);
    LocalDateTime publishedAt = feed.getPublishedAt();

    feed.changeStatus(FeedStatus.REJECTED);
    feed.changeStatus(FeedStatus.PUBLISHED);

    assertThat(feed.getStatus()).isEqualTo(FeedStatus.PUBLISHED);
    assertThat(feed.getPublishedAt()).isEqualTo(publishedAt);
  }

  @ParameterizedTest
  @EnumSource(
      value = FeedStatus.class,
      names = {"PENDING", "HIDDEN", "HIDDEN_BY_REPORT"})
  @DisplayName("공개·반려가 아닌 상태로는 바꿀 수 없다")
  void rejectNonReviewStatus(FeedStatus status) {
    TextFeed feed = TextFeed.create(1L, "GOLD", "사진", CENTER, null, IMAGE, KEY);

    assertErrorCode(() -> feed.changeStatus(status), FeedErrorCode.INVALID_FEED_STATUS_CHANGE);
  }

  @Test
  @DisplayName("신고로 숨겨진 피드는 다시 공개할 수 없다")
  void rejectChangingHiddenByReport() {
    TextFeed feed = TextFeedFixture.builder().status(FeedStatus.HIDDEN_BY_REPORT).build();

    assertErrorCode(
        () -> feed.changeStatus(FeedStatus.PUBLISHED), FeedErrorCode.INVALID_FEED_STATUS_CHANGE);
  }
}
