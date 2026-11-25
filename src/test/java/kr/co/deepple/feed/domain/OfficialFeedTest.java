package kr.co.deepple.feed.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.fixture.OfficialFeedFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("관리자 피드 규칙 테스트")
class OfficialFeedTest {

  @Test
  @DisplayName("공개 여부에 따라 공개 또는 숨김 상태로 만든다")
  void statusByPublished() {
    assertThat(OfficialFeedFixture.builder().isPublished(true).build().getStatus())
        .isEqualTo(FeedStatus.PUBLISHED);
    assertThat(OfficialFeedFixture.builder().isPublished(false).build().getStatus())
        .isEqualTo(FeedStatus.HIDDEN);
  }

  @Test
  @DisplayName("대상 등급이 없으면 모든 등급에, 있으면 그 등급에만 보인다")
  void visibleToTargetGrades() {
    OfficialFeed all = OfficialFeedFixture.builder().build();
    OfficialFeed goldOnly = OfficialFeedFixture.builder().targetGrades(Set.of("GOLD")).build();

    assertThat(all.isVisibleTo("SILVER")).isTrue();
    assertThat(goldOnly.isVisibleTo("GOLD")).isTrue();
    assertThat(goldOnly.isVisibleTo("SILVER")).isFalse();
  }

  @Test
  @DisplayName("등급이 없는 조회자에게는 사용자 피드도 관리자 피드도 보이지 않는다")
  void invisibleToViewerWithoutGrade() {
    assertThat(OfficialFeedFixture.builder().build().isVisibleTo(null)).isFalse();
    assertThat(TextFeedFixture.builder().grade("GOLD").build().isVisibleTo(null)).isFalse();
  }

  @Test
  @DisplayName("홈에 보일 등급은 대상 등급이 없으면 모든 등급, 있으면 대상 등급이다")
  void visibleGrades() {
    assertThat(OfficialFeedFixture.builder().build().getVisibleGrades())
        .containsExactlyInAnyOrder("DIAMOND", "GOLD", "SILVER");
    assertThat(
            OfficialFeedFixture.builder().targetGrades(Set.of("GOLD")).build().getVisibleGrades())
        .containsExactly("GOLD");
  }

  @Test
  @DisplayName("제목 50자, 내용 200자를 넘거나 이미지가 1~5장이 아니면 거절한다")
  void validateLimits() {
    assertErrorCode(
        OfficialFeedFixture.builder().title("가".repeat(51)), FeedErrorCode.INVALID_FEED_CONTENT);
    assertErrorCode(OfficialFeedFixture.builder().title(" "), FeedErrorCode.INVALID_FEED_CONTENT);
    assertErrorCode(
        OfficialFeedFixture.builder().content("가".repeat(201)), FeedErrorCode.INVALID_FEED_CONTENT);
    assertErrorCode(
        OfficialFeedFixture.builder().images(images(6)), FeedErrorCode.INVALID_FEED_IMAGES);
    assertErrorCode(
        OfficialFeedFixture.builder().images(List.of()), FeedErrorCode.INVALID_FEED_IMAGES);
  }

  @Test
  @DisplayName("대상 등급은 회원 등급 값만 쓸 수 있다")
  void validateTargetGrades() {
    assertErrorCode(
        OfficialFeedFixture.builder().targetGrades(Set.of("BRONZE")),
        FeedErrorCode.INVALID_FEED_TARGET_GRADES);

    OfficialFeed feed = OfficialFeedFixture.builder().build();
    assertThatThrownBy(() -> feed.changeTargetGrades(Set.of("GOLD", "BRONZE")))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_TARGET_GRADES);
  }

  @Test
  @DisplayName("공개와 숨김 사이로만 바꿀 수 있고, 다시 공개해도 처음 공개 시각을 유지한다")
  void changeStatusBetweenPublishedAndHidden() {
    LocalDateTime publishedAt = LocalDateTime.of(2026, 11, 1, 9, 0);
    OfficialFeed feed = OfficialFeedFixture.builder().publishedAt(publishedAt).build();

    feed.changeStatus(FeedStatus.HIDDEN);
    feed.changeStatus(FeedStatus.PUBLISHED);

    assertThat(feed.getStatus()).isEqualTo(FeedStatus.PUBLISHED);
    assertThat(feed.getPublishedAt()).isEqualTo(publishedAt);
  }

  @Test
  @DisplayName("숨긴 채 등록한 피드는 처음 공개할 때 공개 시각이 생긴다")
  void setPublishedAtOnFirstPublish() {
    OfficialFeed feed = OfficialFeedFixture.builder().isPublished(false).build();
    assertThat(feed.getPublishedAt()).isNull();

    feed.changeStatus(FeedStatus.PUBLISHED);

    assertThat(feed.getPublishedAt()).isNotNull();
  }

  @ParameterizedTest
  @EnumSource(
      value = FeedStatus.class,
      names = {"PENDING", "REJECTED", "HIDDEN_BY_REPORT"})
  @DisplayName("공개·숨김이 아닌 상태로는 바꿀 수 없다")
  void rejectOtherStatus(FeedStatus status) {
    OfficialFeed feed = OfficialFeedFixture.builder().build();

    assertThatThrownBy(() -> feed.changeStatus(status))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_STATUS_CHANGE);
  }

  @Test
  @DisplayName("이미지는 보낸 순서대로 바꾼다")
  void changeImagesInRequestOrder() {
    List<FeedImage> current = images(3);
    OfficialFeed feed = OfficialFeedFixture.builder().images(current).build();
    FeedImage added = FeedImage.of(10L, "key10", "thumb10", 0);

    feed.changeImages(List.of(added, current.get(2), current.get(0)));

    assertThat(feed.getImageIds()).containsExactly(10L, 2L, 0L);
  }

  @Test
  @DisplayName("이미지를 모두 지우거나 5장을 넘게 바꿀 수 없다")
  void rejectInvalidImageChange() {
    OfficialFeed feed = OfficialFeedFixture.builder().build();

    assertThatThrownBy(() -> feed.changeImages(List.of()))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_IMAGES);
    assertThatThrownBy(() -> feed.changeImages(images(6)))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_IMAGES);
  }

  private static List<FeedImage> images(int count) {
    return IntStream.range(0, count)
        .mapToObj(index -> FeedImage.of((long) index, "key" + index, "thumb" + index, index))
        .toList();
  }

  private static void assertErrorCode(OfficialFeedFixture fixture, FeedErrorCode errorCode) {
    assertThatThrownBy(fixture::build)
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(errorCode);
  }
}
