package kr.co.deepple.feed.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.fixture.OfficialFeedFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
