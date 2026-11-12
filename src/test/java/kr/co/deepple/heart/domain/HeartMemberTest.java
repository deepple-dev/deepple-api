package kr.co.deepple.heart.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.heart.fixture.HeartMemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HeartMemberTest {

  private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  @DisplayName("더 나중에 발생한 변경이면 성별과 VIP 여부를 덮어쓴다.")
  void updateWhenSourceIsNewer() {
    // given
    HeartMember heartMember =
        HeartMemberFixture.builder().gender(Gender.MALE).isVip(false).sourceUpdatedAt(BASE).build();

    // when
    heartMember.update(Gender.FEMALE, true, BASE.plusSeconds(1));

    // then
    assertThat(heartMember.getGender()).isEqualTo(Gender.FEMALE);
    assertThat(heartMember.isVip()).isTrue();
    assertThat(heartMember.getSourceUpdatedAt()).isEqualTo(BASE.plusSeconds(1));
  }

  @Test
  @DisplayName("이미 반영한 것보다 오래된 변경은 무시한다.")
  void ignoreWhenSourceIsOlder() {
    // given
    HeartMember heartMember =
        HeartMemberFixture.builder().gender(Gender.MALE).isVip(true).sourceUpdatedAt(BASE).build();

    // when
    heartMember.update(Gender.FEMALE, false, BASE.minusSeconds(1));

    // then
    assertThat(heartMember.getGender()).isEqualTo(Gender.MALE);
    assertThat(heartMember.isVip()).isTrue();
    assertThat(heartMember.getSourceUpdatedAt()).isEqualTo(BASE);
  }
}
