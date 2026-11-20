package kr.co.deepple.feed.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FeedAuthorTest {

  private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  @DisplayName("처음 오는 프로필 변경은 그대로 반영한다.")
  void updateProfileWhenFirst() {
    // given
    FeedAuthor author = FeedAuthorFixture.builder().build();

    // when
    author.updateProfile("진저", "FEMALE", 1998, "SEOUL", "ENFP", BASE);

    // then
    assertThat(author.getNickname()).isEqualTo("진저");
    assertThat(author.getGender()).isEqualTo("FEMALE");
    assertThat(author.getYearOfBirth()).isEqualTo(1998);
    assertThat(author.getCity()).isEqualTo("SEOUL");
    assertThat(author.getMbti()).isEqualTo("ENFP");
    assertThat(author.getProfileUpdatedAt()).isEqualTo(BASE);
  }

  @Test
  @DisplayName("이미 반영한 것보다 오래되거나 같은 시각의 프로필 변경은 무시한다.")
  void ignoreOlderProfile() {
    // given
    FeedAuthor author = FeedAuthorFixture.builder().build();
    author.updateProfile("진저", "FEMALE", 1998, "SEOUL", "ENFP", BASE);

    // when
    author.updateProfile("옛닉네임", "MALE", 1990, "BUSAN", "ISTJ", BASE.minusSeconds(1));
    author.updateProfile("같은시각", "MALE", 1990, "BUSAN", "ISTJ", BASE);

    // then
    assertThat(author.getNickname()).isEqualTo("진저");
    assertThat(author.getProfileUpdatedAt()).isEqualTo(BASE);
  }

  @Test
  @DisplayName("항목 묶음마다 반영 시각을 따로 비교해, 다른 항목의 더 새로운 변경 뒤에 온 이벤트도 반영한다.")
  void compareUpdatedAtPerGroup() {
    // given
    FeedAuthor author = FeedAuthorFixture.builder().build();
    author.updateProfile("진저", "FEMALE", 1998, "SEOUL", "ENFP", BASE.plusSeconds(10));

    // when
    author.updateProfileImage("https://cdn.example.com/1.webp", BASE);
    author.updatePersonalityType("GROWING_RUNNING_MATE", BASE);
    author.updateActivityStatus("ACTIVE", BASE);
    author.updateGrade("GOLD", BASE);

    // then
    assertThat(author.getProfileImageUrl()).isEqualTo("https://cdn.example.com/1.webp");
    assertThat(author.getPersonalityType()).isEqualTo("GROWING_RUNNING_MATE");
    assertThat(author.getActivityStatus()).isEqualTo("ACTIVE");
    assertThat(author.getGrade()).isEqualTo("GOLD");
  }

  @Test
  @DisplayName("오래된 활동 상태 변경은 무시한다.")
  void ignoreOlderActivityStatus() {
    // given
    FeedAuthor author =
        FeedAuthorFixture.builder().activityStatus("DORMANT").activityStatusUpdatedAt(BASE).build();

    // when
    author.updateActivityStatus("ACTIVE", BASE.minusSeconds(1));

    // then
    assertThat(author.getActivityStatus()).isEqualTo("DORMANT");
    assertThat(author.isActive()).isFalse();
  }

  @Test
  @DisplayName("대표 사진을 지우는 변경도 반영한다.")
  void clearProfileImage() {
    // given
    FeedAuthor author = FeedAuthorFixture.builder().build();
    author.updateProfileImage("https://cdn.example.com/1.webp", BASE);

    // when
    author.updateProfileImage(null, BASE.plusSeconds(1));

    // then
    assertThat(author.getProfileImageUrl()).isNull();
  }
}
