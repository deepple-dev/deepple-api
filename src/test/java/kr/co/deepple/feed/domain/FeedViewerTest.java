package kr.co.deepple.feed.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Set;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("피드 조회자 규칙 테스트")
class FeedViewerTest {

  private static final FeedViewer VIEWER = new FeedViewer(1L, "GOLD", Set.of(3L, 4L));

  @Test
  @DisplayName("활동 중이고 차단 관계가 아닌 회원의 피드는 볼 수 있다")
  void canSeeActiveMember() {
    FeedAuthor author = FeedAuthorFixture.builder().memberId(2L).activityStatus("ACTIVE").build();

    assertThat(VIEWER.canSeeAuthor(author)).isTrue();
  }

  @Test
  @DisplayName("비활성화 회원이나 차단 관계인 회원의 피드는 볼 수 없다")
  void cannotSeeInactiveOrBlockedMember() {
    FeedAuthor dormant = FeedAuthorFixture.builder().memberId(2L).activityStatus("DORMANT").build();
    FeedAuthor blocked = FeedAuthorFixture.builder().memberId(3L).activityStatus("ACTIVE").build();

    assertThat(VIEWER.canSeeAuthor(dormant)).isFalse();
    assertThat(VIEWER.canSeeAuthor(blocked)).isFalse();
  }

  @Test
  @DisplayName("공식 계정의 피드는 활동 상태·차단과 상관없이 볼 수 있다")
  void canSeeOfficial() {
    FeedAuthor official = FeedAuthorFixture.builder().memberId(4L).build();
    setField(official, "authorType", FeedAuthorType.OFFICIAL);

    assertThat(VIEWER.canSeeAuthor(official)).isTrue();
  }

  @Test
  @DisplayName("차단 관계는 활동 상태와 상관없이 회원 ID로만 판단한다")
  void isBlockedWithMember() {
    FeedAuthor dormant = FeedAuthorFixture.builder().memberId(2L).activityStatus("DORMANT").build();
    FeedAuthor blocked = FeedAuthorFixture.builder().memberId(3L).activityStatus("ACTIVE").build();

    assertThat(VIEWER.isBlockedWith(dormant)).isFalse();
    assertThat(VIEWER.isBlockedWith(blocked)).isTrue();
  }

  @Test
  @DisplayName("회원 ID가 없는 공식 계정과는 차단 관계가 아니다")
  void isNotBlockedWithOfficial() {
    assertThat(VIEWER.isBlockedWith(FeedAuthorFixture.official())).isFalse();
  }
}
