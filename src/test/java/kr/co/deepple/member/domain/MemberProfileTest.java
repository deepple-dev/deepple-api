package kr.co.deepple.member.domain;

import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.fixture.MemberProfileFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MemberProfileTest {

  @DisplayName("값이 하나라도 Null 이 있다면, 프로필 업데이트가 필요한 대상이다.")
  @Test
  void isNeededProfileSettingWhenNullValueExists() {
    // Given
    MemberProfile memberProfile = MemberProfileFixture.builder().district(null).build();

    // When & Then
    Assertions.assertThat(memberProfile.isProfileSettingNeeded()).isTrue();
  }

  @DisplayName("취미 목록이 비어있는 경우에, 프로필 업데이트가 필요한 대상이다.")
  @Test
  void isNeededProfileSettingWhenHobbyListIsEmpty() {
    // Given
    MemberProfile memberProfile = MemberProfileFixture.builder().hobbies(Set.of()).build();

    // When & Then
    Assertions.assertThat(memberProfile.isProfileSettingNeeded()).isTrue();
  }

  @DisplayName("취미 목록이 비어있지 않고, 모든 값이 Null 이 아닌 경우, 프로필 업데이트가 필요하지 않은 대상이다.")
  @Test
  void isNeededProfileSettingWhenHobbyListIsNotEmptyAndNullValueDoesntNotExist() {
    // Given
    MemberProfile memberProfile =
        MemberProfileFixture.builder().hobbies(Set.of(Hobby.ANIMATION)).build();

    // When & Then
    Assertions.assertThat(memberProfile.isProfileSettingNeeded()).isFalse();
  }

  @DisplayName("키가 130 이상 220 이하인 경우, 정상 생성")
  @ParameterizedTest
  @ValueSource(ints = {130, 220})
  void createWhenHeightIsInRange(int height) {
    // When
    MemberProfile memberProfile = MemberProfileFixture.builder().height(height).build();

    // Then
    Assertions.assertThat(memberProfile.getHeight()).isEqualTo(height);
  }

  @DisplayName("키가 130 미만이거나 220 초과인 경우, 예외 발생")
  @ParameterizedTest
  @ValueSource(ints = {129, 221})
  void throwExceptionWhenHeightIsOutOfRange(int height) {
    // Given
    MemberProfileFixture fixture = MemberProfileFixture.builder().height(height);

    // When & Then
    Assertions.assertThatThrownBy(fixture::build)
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_HEIGHT);
  }

  @DisplayName("키가 null 인 경우, 정상 생성")
  @Test
  void createWhenHeightIsNull() {
    // When
    MemberProfile memberProfile = MemberProfileFixture.builder().height(null).build();

    // Then
    Assertions.assertThat(memberProfile.getHeight()).isNull();
  }

  @DisplayName("취미가 3개인 경우, 정상 생성")
  @Test
  void createWhenHobbyCountIsThree() {
    // Given
    Set<Hobby> hobbies = Set.of(Hobby.TRAVEL, Hobby.ANIMATION, Hobby.BOARD_GAMES);

    // When
    MemberProfile memberProfile = MemberProfileFixture.builder().hobbies(hobbies).build();

    // Then
    Assertions.assertThat(memberProfile.getHobbies()).hasSize(3);
  }

  @DisplayName("취미가 4개 이상인 경우, 예외 발생")
  @Test
  void throwExceptionWhenHobbyCountExceedsThree() {
    // Given
    MemberProfileFixture fixture =
        MemberProfileFixture.builder()
            .hobbies(
                Set.of(Hobby.TRAVEL, Hobby.ANIMATION, Hobby.BOARD_GAMES, Hobby.WEBTOON_AND_COMICS));

    // When & Then
    Assertions.assertThatThrownBy(fixture::build)
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.HOBBY_COUNT_EXCEEDED);
  }
}
