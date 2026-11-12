package kr.co.deepple.member.command.application.member;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.command.domain.member.PrimaryContactType;
import kr.co.deepple.member.fixture.MemberFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MemberContactServiceTest {

  @Mock private MemberCommandRepository memberCommandRepository;

  @InjectMocks private MemberContactService memberContactService;

  @Nested
  class UpdateTest {

    @Nested
    @DisplayName("휴대폰 번호 변경 테스트")
    class PhoneNumberUpdateTest {

      Member member = MemberFixture.builder().id(1L).phoneNumber("01012345678").build();
      Member anotherMember = MemberFixture.builder().id(2L).phoneNumber("01098765432").build();

      @Test
      @DisplayName("변경한 핸드폰 번호가 다른 멤버가 사용중인 핸드폰 번호인 경우 실패.")
      void isFailWhenUpdatedNumberIsAnotherMemberPhoneNumber() {
        // Given
        String updatePhoneNumber = anotherMember.getPhoneNumber();

        Mockito.when(
                memberCommandRepository.existsByPhoneNumberAndIdNot(
                    Mockito.any(), Mockito.anyLong()))
            .thenReturn(true);

        // When & Then
        Assertions.assertThatThrownBy(
                () -> memberContactService.updatePhoneNumber(member.getId(), updatePhoneNumber))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
      }

      @Test
      @DisplayName("아무도 사용하지 않는 핸드폰 번호인 경우 성공.")
      void isSuccessWhenUpdateNumberIsNotAnotherMemberPhoneNumber() {
        // Given
        String updatedPhoneNumber = "01099999999";

        Mockito.when(
                memberCommandRepository.existsByPhoneNumberAndIdNot(
                    Mockito.any(), Mockito.anyLong()))
            .thenReturn(false);
        Mockito.when(memberCommandRepository.findById(member.getId()))
            .thenReturn(Optional.of(member));

        // When
        memberContactService.updatePhoneNumber(member.getId(), updatedPhoneNumber);

        // Then
        Assertions.assertThat(member.getPhoneNumber()).isEqualTo(updatedPhoneNumber);
      }

      @Test
      @DisplayName("기존 자신의 핸드폰 번호인 경우 성공.")
      void isSuccessWhenUpdateNumberIsOwnPhoneNumber() {
        // Given
        String updatedPhoneNumber = member.getPhoneNumber();

        Mockito.when(
                memberCommandRepository.existsByPhoneNumberAndIdNot(
                    Mockito.any(), Mockito.anyLong()))
            .thenReturn(false);
        Mockito.when(memberCommandRepository.findById(member.getId()))
            .thenReturn(Optional.of(member));

        // When
        memberContactService.updatePhoneNumber(member.getId(), updatedPhoneNumber);

        // Then
        Assertions.assertThat(member.getPhoneNumber()).isEqualTo(updatedPhoneNumber);
      }
    }

    @Nested
    @DisplayName("카카오 아이디 업데이트 테스트")
    class KakaoIdUpdateTest {

      Member member = MemberFixture.builder().id(1L).phoneNumber("01012345678").build();

      @Test
      @DisplayName("변경한 카카오 아이디가 다른 멤버가 사용중인 경우 실패.")
      void isFailWhenUpdatedKakaoIdIsAnotherMemberKakaoId() {
        // Given
        String kakaoId = "kakaoId";

        Mockito.when(
                memberCommandRepository.existsByKakaoIdAndIdNot(Mockito.any(), Mockito.anyLong()))
            .thenReturn(true);

        // When & Then
        Assertions.assertThatThrownBy(
                () -> memberContactService.updateKakaoId(member.getId(), kakaoId))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.KAKAO_ID_ALREADY_EXISTS);
      }

      @Test
      @DisplayName("아무도 사용하지 않은 카카오 아이디인 경우 성공.")
      void isSuccessWhenUpdatedKakaoIdIsNotAnotherMemberKakaoId() {
        // Given
        String kakaoId = "kakaoId";

        Mockito.when(
                memberCommandRepository.existsByKakaoIdAndIdNot(Mockito.any(), Mockito.anyLong()))
            .thenReturn(false);
        Mockito.when(memberCommandRepository.findById(member.getId()))
            .thenReturn(Optional.of(member));

        // When
        memberContactService.updateKakaoId(member.getId(), kakaoId);

        // Then
        Assertions.assertThat(member.getKakaoId()).isEqualTo(kakaoId);
      }

      @Test
      @DisplayName("기존 자신의 카카오 아이디인 경우 성공.")
      void isSuccessWhenUpdatedKakaoIdIsOwnKakaoId() {
        // Given
        String kakaoId = "kakaoId";
        member =
            MemberFixture.builder()
                .id(1L)
                .phoneNumber("01012345678")
                .kakaoId(kakaoId)
                .primaryContactType(PrimaryContactType.KAKAO)
                .build();

        Mockito.when(
                memberCommandRepository.existsByKakaoIdAndIdNot(Mockito.any(), Mockito.anyLong()))
            .thenReturn(false);
        Mockito.when(memberCommandRepository.findById(member.getId()))
            .thenReturn(Optional.of(member));

        // When
        memberContactService.updateKakaoId(member.getId(), kakaoId);

        // Then
        Assertions.assertThat(member.getKakaoId()).isEqualTo(kakaoId);
      }
    }
  }
}
