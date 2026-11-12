package kr.co.deepple.member.command.domain.member;

import kr.co.deepple.member.command.domain.member.vo.MemberProfile;
import kr.co.deepple.member.fixture.MemberProfileFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

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
        MemberProfile memberProfile = MemberProfileFixture.builder().hobbies(Set.of(Hobby.ANIMATION)).build();

        // When & Then
        Assertions.assertThat(memberProfile.isProfileSettingNeeded()).isFalse();
    }

}
